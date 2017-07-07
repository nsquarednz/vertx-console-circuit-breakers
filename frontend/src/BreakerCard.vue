<template>
    <div class="col-xs-12 col-sm-6 col-md-4 col-lg-3">
        <div class="card-pf card-pf-view card-pf-view-select card-pf-view-single-select breaker-card" :class="statusCardClass">
            <div class="card-pf-body">
                <div class="breaker-data" @mouseover="displayOverlay = true" :class="{ 'below-overlay': displayOverlay}">
                    <div class="data-row">
                        <h2 class="card-pf-title breaker-name">{{ breaker.name }}</h2>
                        <div class="breaker-state">{{ breaker.state.replace('_', ' ') }}</div>
                    </div>
                    <div class="data-row">
                        <span>{{ abbreviate(breaker.operationRate, 1) }} {{ plural('op', breaker.operationRate) }}/sec</span>
                        <span style="float: right">
                            <b>{{ breaker.rollingErrorPercentage }}% rate</b>
                            <i class="fa fa-exclamation-triangle" aria-hidden="true"></i>
                        </span>
                    </div>
                    <div class="data-row">
                        <span>{{ abbreviate(breaker.rollingOperationCount, 1) }} {{ plural('call', breaker.rollingOperationCount) }} in last {{ prettyMs(breaker.metricRollingWindow) }}</span>
                        <span style="float: right">
                            <b>{{ prettyMs(breaker.rollingLatencyMean) }} avg</b>
                            <i class="fa fa-clock-o" aria-hidden="true"></i>
                        </span>
                    </div>
                </div>
                <div class="rate-chart" :class="{ 'below-overlay': displayOverlay}">
                    <pf-sparkline :tooltipContents="tooltipContents" :maxDisplayed="20" :data="operationRate" :extraChartOptions="extraChartOptions"></pf-sparkline>
                </div>
                <div class="toggle-overlay" :class="{ shown: displayOverlay }" @mouseleave="displayOverlay = false" @click="onClick">
                    <div class="data-row">
                        <h2 class="card-pf-title breaker-name">{{ breaker.name }}</h2>
                        <div class="breaker-state">LAST {{ prettyMs(breaker.metricRollingWindow).toUpperCase() }}</div>
                    </div>
                    <table class="details-table" style="display: inline-block;">
                        <tbody>
                            <tr>
                                <td>Successes</td>
                                <td>{{ abbreviate(breaker.rollingSuccessCount, 1) }}</td>
                            </tr>
                            <tr>
                                <td>Failures</td>
                                <td>{{ abbreviate(breaker.rollingFailureCount, 1) }}</td>
                            </tr>
                            <tr>
                                <td>Exceptions</td>
                                <td>{{ abbreviate(breaker.rollingExceptionCount, 1) }}</td>
                            </tr>
                            <tr>
                                <td>Timeouts</td>
                                <td>{{ abbreviate(breaker.rollingTimeoutCount, 1) }}</td>
                            </tr>
                            <tr>
                                <td>Rejections</td>
                                <td>{{ abbreviate(breaker.rollingShortCircuitedCount, 1) }}</td>
                            </tr>
                        </tbody>
                    </table>
                    <table class="details-table" style="float: right">
                        <tbody>
                            <tr>
                                <td>Median</td>
                                <td>{{ prettyMs(breaker.rollingLatency['50']) }}</td>
                            </tr>
                            <tr>
                                <td>Max</td>
                                <td>{{ prettyMs(breaker.rollingLatency['100']) }}</td>
                            </tr>
                            <tr>
                                <td>90th</td>
                                <td>{{ prettyMs(breaker.rollingLatency['90']) }}</td>
                            </tr>
                            <tr>
                                <td>95th</td>
                                <td>{{ prettyMs(breaker.rollingLatency['95']) }}</td>
                            </tr>
                            <tr>
                                <td>99th</td>
                                <td>{{ prettyMs(breaker.rollingLatency['99']) }}</td>
                            </tr>
                        </tbody>
                    </table>
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
    data() {
        return {
            displayOverlay: false
        }
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
    methods: {
        onClick() {
            // console.log('clicked')
        },
        plural(noun, amount) {
            return amount === 1 ? noun : noun + 's';
        }
    },
    computed: {
        operationRate() {
            // console.log(JSON.stringify(this.breaker));
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
}

.data-row {
    padding: 0 $card-margin;
}

.rate-chart {
    margin-bottom: -3px;
}

.breaker-data,
.rate-chart {
    transition: filter 0.2s;
}

.below-overlay {
    opacity: 0.4;
    filter: blur(3px);
}

.toggle-overlay {
    position: absolute;
    width: 100%;
    height: 100%;
    pointer-events: none;
    opacity: 0;
    transition: all 0.2s;
    padding: $card-margin;
    &.shown {
        opacity: 1;
        pointer-events: auto;
    }

    .data-row {
        padding: 0;
    }
}

.details-table {
    margin-top: 2px;

    td:first-child {
        font-weight: bold;
        padding-right: 6px;
        border-right: 1px solid rgba(255, 255, 255, 0.2);
    }

    td:last-child {
        padding-left: 6px;
    }
}
</style>