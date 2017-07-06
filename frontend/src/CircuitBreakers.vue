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

.breaker-filter {
    margin: 0 -10px 20px;
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
        </div>
        <transition-group name="breaker-list" tag="div" class="row">
            <breaker-card v-for="breaker in sortedBreakers" :key="breaker.name" :breaker="breaker" v-show="isDisplayed(breaker.name)"></breaker-card>
        </transition-group>
    </div>
</template>

<script>
import EventBus from 'vertx3-eventbus-client';
import BreakerCard from './BreakerCard.vue';

export default {
    name: 'Circuit Breakers',
    components: {
        'breaker-card': BreakerCard
    },
    data() {
        return {
            breakers: {},
            filterQuery: ''
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
            const sorted = Object.keys(this.breakers)
                .map(k => this.breakers[k])
                .sort((a, b) => {
                    const aLevel = a.stateLevel;
                    const bLevel = b.stateLevel;
                    if (aLevel === bLevel) {
                        const aName = a.name.toUpperCase();
                        const bName = b.name.toUpperCase();
                        if (aName < bName) {
                            return -1;
                        } else if (aName > bName) {
                            return 1;
                        } else {
                            return 0;
                        }
                    } else {
                        return aLevel - bLevel;
                    }
                });
            return sorted;
        },
        processedFilterQuery() {
            return this.filterQuery.trim().toLowerCase();
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
    },
    beforeDestroy() {
        this.eb.close();
    }
}
</script>