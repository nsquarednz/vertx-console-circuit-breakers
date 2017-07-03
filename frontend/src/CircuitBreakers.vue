<template>
    <div>
        <div v-for="msg in msgs" :key="msg">{{ msg }}</div>
    </div>
</template>

<script>
import EventBus from 'vertx3-eventbus-client';

export default {
    data() {
        return {
            msgs: []
        }
    },
    beforeMount() {

        this.$http.get(window.location.pathname + '/circuitbreakers/address')
            .then(response => response.json())
            .then(response => {
                this.eb = new EventBus(window.location.pathname + '/circuitbreakerproxy');
                this.eb.onopen = () => {
                    this.eb.registerHandler(response.address, (e, m) => {
                        this.msgs.push(m.body);
                    });
                };
            });
    }
}
</script>