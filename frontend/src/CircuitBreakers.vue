<style lang="scss" scoped>
.breaker-container {
    overflow-x: hidden;

    &>.row {
        padding: 0 10px;
        &:first-child {
            padding-top: 20px;
        }
    }
}

.breaker-list-enter-active,
.breaker-list-leave-active {
    transition: all 0.5s;
    &>.card {
        opacity: 0;
        transform: scale(0.25, 0.25);
    }
}

.breaker-list-leave-active {
    position: absolute;
    z-index: -1;
    &>.card {
        transition: all 0.2s;
    }
}

.breaker-list-move {
    transition: transform 0.2s ease-in-out;
}

.breaker-filter,
.breaker-sort {
    margin: 0 -10px 20px;
}

.breaker-sort {
    float: right;

    .btn-group.bootstrap-select {
        width: 120px;
    }
}
</style>

<template>
    <div class="container-fluid breaker-container">
        <div class="row breaker-toolbar">
            <div class="col-xs-12 col-sm-6 col-md-4 col-lg-3">
                <div class="breaker-filter">
                    <label for="filter" class="sr-only">Filter</label>
                    <input type="text" id="filter" class="form-control" placeholder="Filter" v-model="filterQuery">
                </div>
            </div>
            <div class="col-xs-12 col-sm-offset-3 col-sm-3 col-md-offset-6 col-md-2 col-lg-offset-7 col-lg-2">
                <div class="breaker-sort">
                    <v-select v-model="sortType" :options="sortOptions"></v-select>
                </div>
            </div>
        </div>
        <transition-group :name="filtering ? null : 'breaker-list'" tag="div" class="row">
            <breaker-card v-for="breaker in sortedBreakers" :key="breaker.name" :breaker="breaker" v-show="isDisplayed(breaker.name)"></breaker-card>
        </transition-group>
    </div>
</template>

<script>
import EventBus from 'vertx3-eventbus-client';
import BreakerCard from './BreakerCard.vue';

const STATE_SORT = 'Sort by State';
const NAME_SORT = 'Sort by Name';
const CALL_SORT = 'Sort by Calls';
const RATE_SORT = 'Sort by Rate';

export default {
    name: 'Circuit Breakers',
    components: {
        'breaker-card': BreakerCard
    },
    data() {
        return {
            breakers: {},
            filterQuery: '',
            filtering: false,
            sortType: STATE_SORT
        }
    },
    methods: {
        isDisplayed(name) {
            if (this.processedFilterQuery === '') {
                return true;
            } else {
                return name.toLowerCase().includes(this.processedFilterQuery);
            }
        }
    },
    computed: {
        sortedBreakers() {
            const breakerValues = Object.keys(this.breakers)
                .map(k => this.breakers[k]);
            const nameComparator = (a, b) => {
                    const aName = a.name.toUpperCase();
                    const bName = b.name.toUpperCase();
                    if (aName < bName) {
                        return -1;
                    } else if (aName > bName) {
                        return 1;
                    } else {
                        return 0;
                    }
                };

            if (this.sortType === STATE_SORT) {
                breakerValues.sort((a, b) => {
                    const aLevel = a.stateLevel;
                    const bLevel = b.stateLevel;
                    if (aLevel === bLevel) {
                        return nameComparator(a, b);
                    } else {
                        return aLevel - bLevel;
                    }
                });
            } else if (this.sortType === NAME_SORT) {
                breakerValues.sort(nameComparator);
            } else if (this.sortType === CALL_SORT) {
                breakerValues.sort((a, b) => b.rollingOperationCount - a.rollingOperationCount);
            } else {
                breakerValues.sort((a, b) => b.operationRate - a.operationRate);
            }
            return breakerValues;
        },
        processedFilterQuery() {
            return this.filterQuery.trim().toLowerCase();
        }
    },
    watch: {
        filterQuery() {
            // Disable animations when filtering
            this.filtering = true;
            this.$nextTick(() => this.filtering = false);
        }
    },
    beforeMount() {
        const getAddress = this.$http.get(window.location.pathname + '/circuitbreakers/address')
            .then(response => response.json());
        const openEventBus = new Promise((resolve, reject) => {
            this.eb = new EventBus(window.location.pathname + '/circuitbreakerproxy');
            this.eb.onopen = () => resolve(true);
            this.eb.onerror = err => reject(err);
        });
        Promise.all([getAddress, openEventBus]).then(values => {
            this.eb.registerHandler(values[0].address, (e, m) => {
                const breaker = m.body;
                breaker.lastUpdated = performance.now();
                if (breaker.state === 'OPEN') {
                    breaker.stateLevel = 0;
                } else if (breaker.state === 'HALF_OPEN') {
                    breaker.stateLevel = 1;
                } else {
                    breaker.stateLevel = 2;
                }
                const previous = this.breakers[breaker.name];
                if (previous) {
                    breaker.operationRate = Math.round((breaker.totalOperationCount - previous.totalOperationCount) * 1000 / (breaker.lastUpdated - previous.lastUpdated));
                } else {
                    breaker.operationRate = 0;
                }
                this.$set(this.breakers, breaker.name, breaker);
            });
        });

        this.sortOptions = [STATE_SORT, NAME_SORT, CALL_SORT, RATE_SORT];
    },
    beforeDestroy() {
        this.eb.close();
    }
}
</script>