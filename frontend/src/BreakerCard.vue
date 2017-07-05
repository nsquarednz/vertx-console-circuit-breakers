<template>
    <div class="col-xs-12 col-sm-6 col-md-4 col-lg-3">
        <div class="card-pf card-pf-view card-pf-view-select card-pf-view-single-select breaker-card">
            <div class="card-pf-body">
                <h2 class="card-pf-title breaker-name">{{ breaker.name }}</h2>
                <div class="breaker-state">{{ breaker.state.replace('_', ' ') }}</div>
                <div class="rate-chart">
                    <pf-sparkline :tooltipContents="tooltipContents" :maxDisplayed="20" :data="operationRate"></pf-sparkline>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import abbreviate from 'number-abbreviate';

export default {
    props: {
        breaker: Object
    },
    created() {
        this.tooltipContents = { contents: d => '<span class="c3-tooltip-sparkline">' + abbreviate(d[0].value, 1) + ' Ops/sec</span>'}
    },
    computed: {
        operationRate() {
            return { indices: [new Date()], values: [this.breaker.operationRate] };
        }
    }
}
</script>

<style lang="scss" scoped>
.breaker-card .card-pf-body {
    height: 150px;
}

.breaker-name {
    font-size: 16px;
    margin-top: 0;
}

.breaker-state {
    float: right;
    font-weight: bold;
}

.breaker-name,
.breaker-state {
    display: inline-block;
}

.rate-chart {
    position: absolute;
    bottom: 0;
    left: 0;
}
</style>