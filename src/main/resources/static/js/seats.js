/* =============================================================================
   CineX - Chọn ghế
   ============================================================================= */
(function () {
    'use strict';

    const seats = Array.from(document.querySelectorAll('.seat'));
    const counter = document.getElementById('seat-count');
    const totalEl = document.getElementById('seat-total');
    const submitBtn = document.getElementById('checkout-btn');
    const inputs = document.getElementById('seat-inputs');

    const formatVnd = (value) => value.toLocaleString('vi-VN') + ' đ';

    function refresh() {
        const selected = seats.filter((seat) => seat.classList.contains('selected'));
        const total = selected.reduce((sum, seat) => sum + Number(seat.dataset.price || 0), 0);

        counter.textContent = selected.length;
        totalEl.textContent = formatVnd(total);
        submitBtn.disabled = selected.length === 0;

        inputs.innerHTML = '';
        selected.forEach((seat) => {
            const input = document.createElement('input');
            input.type = 'hidden';
            input.name = 'seatIds';
            input.value = seat.dataset.seatId;
            inputs.appendChild(input);
        });
    }

    seats.forEach((seat) => {
        seat.addEventListener('click', () => {
            if (seat.classList.contains('taken')) {
                return;
            }
            seat.classList.toggle('selected');
            refresh();
        });
    });

    refresh();
})();
