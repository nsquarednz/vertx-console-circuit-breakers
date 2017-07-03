<template>
    <div>
        <div v-for="(breaker, name) in breakers" :key="name">{{ breaker }}</div>
    </div>
</template>

<script>
import EventBus from 'vertx3-eventbus-client';

export default {
    data() {
        return {
            breakers: {}
        }
    },
    beforeMount() {
        this.$http.get(window.location.pathname + '/circuitbreakers/address')
            .then(response => response.json())
            .then(response => {
                this.eb = new EventBus(window.location.pathname + '/circuitbreakerproxy');
                this.eb.onopen = () => {
                    this.eb.registerHandler(response.address, (e, m) => {
                        const breaker = m.body;
                        this.$set(this.breakers, breaker.name, breaker);
                    });
                };
            });
    }
}
</script>