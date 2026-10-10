/*
 * Data visualisation module (client side).
 * Draws every chart registered in window.sdemCharts with Chart.js and wires up
 * the Line / Bar switch buttons. Data arrives already calculated by the server.
 */
(function () {
    'use strict';

    // Distinct, colour blind friendly palette (Okabe-Ito based)
    var PALETTE = ['#0072B2', '#E69F00', '#009E73', '#CC79A7', '#56B4E9', '#D55E00', '#F0E442'];
    var instances = {};

    function datasetsFor(chart, type) {
        return chart.datasets.map(function (ds, i) {
            var colour = ds.dashed ? '#1d2a3a' : PALETTE[i % PALETTE.length];
            return {
                label: ds.label,
                data: ds.values,
                borderColor: colour,
                backgroundColor: type === 'bar' ? colour + 'CC' : colour,
                borderWidth: ds.dashed ? 3 : 2,
                borderDash: ds.dashed ? [6, 4] : [],
                pointRadius: 3,
                pointHoverRadius: 6,
                tension: 0.25,
                spanGaps: true
            };
        });
    }

    function draw(id, type) {
        var chart = window.sdemCharts[id];
        var canvas = document.getElementById(id);
        if (!chart || !canvas || typeof Chart === 'undefined') {
            return;
        }
        if (instances[id]) {
            instances[id].destroy();
        }
        instances[id] = new Chart(canvas, {
            type: type,
            data: { labels: chart.labels, datasets: datasetsFor(chart, type) },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                interaction: { mode: 'index', intersect: false },
                plugins: {
                    legend: { position: 'bottom' },
                    tooltip: {
                        callbacks: {
                            label: function (ctx) {
                                var v = ctx.parsed.y;
                                return ctx.dataset.label + ': ' + (v === null ? 'no data' : v.toFixed(1) + ' kWh');
                            }
                        }
                    }
                },
                scales: {
                    y: { beginAtZero: true, title: { display: true, text: chart.unit } },
                    x: { grid: { display: false } }
                }
            }
        });
    }

    document.addEventListener('DOMContentLoaded', function () {
        Object.keys(window.sdemCharts || {}).forEach(function (id) {
            draw(id, 'line');
        });
        document.querySelectorAll('[data-chart]').forEach(function (button) {
            button.addEventListener('click', function () {
                var id = button.getAttribute('data-chart');
                draw(id, button.getAttribute('data-type'));
                document.querySelectorAll('[data-chart="' + id + '"]').forEach(function (b) {
                    b.classList.toggle('active', b === button);
                });
            });
        });
    });
}());
