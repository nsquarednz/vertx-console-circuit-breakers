import CircuitBreakers from './CircuitBreakers.vue';

if (!window.vertxConsoleRoutes) window.vertxConsoleRoutes = [];
window.vertxConsoleRoutes.push({
    path: '/circuitbreakers',
    component: CircuitBreakers
});
