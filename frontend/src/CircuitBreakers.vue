<template>
    <div>
        <div v-for="(breaker, name) in breakers" :key="name">{{ breaker }}</div>
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
                this.$set(this.breakers, breaker.name, breaker);
            });
        });
    },
    beforeDestroy() {
        this.eb.close();
    }
}
</script>