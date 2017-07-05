<template>
    <div class="col-xs-12 col-sm-6 col-md-4 col-lg-3">
        <div class="card-pf card-pf-view card-pf-view-select card-pf-view-single-select breaker-card" :class="statusCardClass">
            <div class="card-pf-body">
                <div class="breaker-data">
                    <div class="data-row">
                        <h2 class="card-pf-title breaker-name">{{ breaker.name }}</h2>
                        <div class="breaker-state">{{ breaker.state.replace('_', ' ') }}</div>
                    </div>
                    <div class="data-row">
                        <span>{{ breaker.operationRate }} ops/sec</span>
                        <span style="float: right">
                            <b>{{ breaker.rollingErrorPercentage }}% rate</b>
                            <i class="fa fa-exclamation-triangle" aria-hidden="true"></i>
                        </span>
                    </div>
                    <div class="data-row">
                        <span>{{ abbreviate(breaker.rollingOperationCount, 1) }} in last {{ prettyMs(breaker.metricRollingWindow) }}</span>
                        <span style="float: right">
                            <b>{{ prettyMs(breaker.rollingLatencyMean) }} avg</b>
                            <i class="fa fa-clock-o" aria-hidden="true"></i>
                        </span>
                    </div>
                </div>
                <div class="rate-chart">
                    <pf-sparkline :tooltipContents="tooltipContents" :maxDisplayed="20" :data="operationRate" :extraChartOptions="extraChartOptions"></pf-sparkline>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import abbreviate from 'number-abbreviate';
import prettyMs from 'pretty-ms';

export default {
    props: {
        breaker: Object
    },
    created() {
        this.tooltipContents = { contents: d => '<span class="c3-tooltip-sparkline">' + abbreviate(d[0].value, 1) + ' ops/sec</span>' };
        this.extraChartOptions = {
            color: {
                pattern: ['#fff']
            }
        };
        this.abbreviate = abbreviate;
        this.prettyMs = prettyMs;
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
    border: none;
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
    margin-top: 0;
}

.breaker-name {
    font-size: 16px;
    margin: 0;
}

.breaker-state {
    float: right;
    font-weight: bold;
    margin-left: $card-margin;
}

.breaker-name,
.breaker-state {
    display: inline-block;
}

.breaker-card .card-pf-body {
    display: -webkit-box;
    display: -ms-flexbox;
    display: flex;
    -webkit-box-orient: vertical;
    -webkit-box-direction: normal;
    -ms-flex-direction: column;
    flex-direction: column;
    height: $card-height - $card-margin;
    background-image: linear-gradient(transparent, rgba(255, 255, 255, 0.125));
}

.breaker-data {
    padding-top: $card-margin;
    flex-grow: 1;

    &:hover {
        background: purple;
    }
}

.data-row {
    padding: 0 $card-margin;
}

.rate-chart {
    margin-bottom: -2px;
}
</style>