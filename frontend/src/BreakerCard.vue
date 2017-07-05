<template>
    <div class="col-xs-12 col-sm-6 col-md-4 col-lg-3">
        <div class="card-pf card-pf-view card-pf-view-select card-pf-view-single-select breaker-card" :class="statusCardClass">
            <div class="card-pf-body">
                <h2 class="card-pf-title breaker-name">{{ breaker.name }}</h2>
                <div class="breaker-state">{{ breaker.state.replace('_', ' ') }}</div>
                <div class="breaker-info">
                    <div class="rate-chart">
                        <pf-sparkline :tooltipContents="tooltipContents" :maxDisplayed="20" :data="operationRate" :extraChartOptions="extraChartOptions"></pf-sparkline>
                    </div>
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
        this.tooltipContents = { contents: d => '<span class="c3-tooltip-sparkline">' + abbreviate(d[0].value, 1) + ' Ops/sec</span>' }
        this.extraChartOptions = {
            color: {
                pattern: ['#fff']
            }
        }
    },
    computed: {
        operationRate() {
            return { indices: [new Date()], values: [this.breaker.operationRate] };
        },
        statusCardClass() {
            if (this.breaker.stateLevel === 0) {
                return 'breaker-card-open';
            } else if (this.breaker.stateLevel === 1) {
                return 'breaker-card-half-open';
            } else {
                return null;
            }
        }
    }
}
</script>

<style lang="scss" scoped>
$card-margin: 10px;
$card-height: 150px;

.breaker-card {
    padding: 0;
    background-color: #3f9c35;
    transition: background-color 2s;
    color: #fff;
    border-left: none;
    border-right: none;
}

.breaker-card-open {
    background-color: #c00;
}

.breaker-card-half-open {
    background-color: #ec7a08;
}

.breaker-card .card-pf-body {
    height: $card-height;
    padding: 0;
    margin-top: $card-margin;
}

.breaker-name {
    font-size: 16px;
    margin-top: 0;
    margin-left: $card-margin;
}

.breaker-state {
    float: right;
    font-weight: bold;
    margin: 0 $card-margin;
}

.breaker-name,
.breaker-state {
    display: inline-block;
}

.breaker-info {
    display: -webkit-box;
    display: -ms-flexbox;
    display: flex;
    -webkit-box-orient: vertical;
    -webkit-box-direction: normal;
    -ms-flex-direction: column;
    flex-direction: column;
    height: $card-height - $card-margin - 8px;
    background-image: linear-gradient(transparent, rgba(255,255,255,0.125));
}

.rate-chart {
    margin-top: auto;
}
</style>