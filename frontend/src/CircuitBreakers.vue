<template>
    <div>
        <div v-for="breaker in sortedBreakers" :key="breaker.name">{{ breaker.name }} : {{ breaker.stateLevel }}</div>
    </div>
</template>

<script>
import EventBus from 'vertx3-eventbus-client';

export default {
    name: 'Circuit Breakers',
    data() {
        return {
            breakers: {}
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
                breaker.lastUpdated = Date.now();
                if (breaker.state === 'OPEN') {
                    breaker.stateLevel = 0;
                } else if (breaker.state === 'HALF_OPEN') {
                    breaker.stateLevel = 1;
                } else {
                    breaker.stateLevel = 2;
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