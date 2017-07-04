<template>
    <div class="col-xs-12 col-sm-6 col-md-4 col-lg-3">
        <div class="card-pf card-pf-view card-pf-view-select card-pf-view-single-select breaker-card">
            <div class="card-pf-body">
                <h2 class="card-pf-title breaker-name">{{ breaker.name }}</h2>
                <div class="breaker-state">{{ breaker.state.replace('_', ' ') }}</div>
            </div>
        </div>
    </div>
</template>

<script>
export default {
    props: {
        breaker: Object
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

.breaker-name, .breaker-state {
    display: inline-block;
}

</style>