/* =============================================================================
   CineX - Slider phim ở phần giới thiệu (hero)
   ============================================================================= */
(function () {
    'use strict';

    document.querySelectorAll('[data-hero-slider]').forEach(function (root) {
        const slides = Array.from(root.querySelectorAll('.hero-slide'));
        if (slides.length === 0) {
            return;
        }

        const prevBtn = root.querySelector('[data-hero-prev]');
        const nextBtn = root.querySelector('[data-hero-next]');
        const dots = Array.from(root.querySelectorAll('[data-hero-dot]'));
        const delay = Number(root.dataset.heroDelay || 6000);

        let index = 0;
        let timer = null;
        let paused = false;

        function show(next) {
            index = (next + slides.length) % slides.length;
            slides.forEach(function (slide, i) {
                slide.classList.toggle('is-active', i === index);
            });
            dots.forEach(function (dot, i) {
                dot.classList.toggle('is-active', i === index);
                dot.setAttribute('aria-selected', i === index ? 'true' : 'false');
            });
        }

        function start() {
            stop();
            timer = window.setInterval(function () {
                if (!paused) {
                    show(index + 1);
                }
            }, delay);
        }

        function stop() {
            if (timer) {
                window.clearInterval(timer);
                timer = null;
            }
        }

        if (nextBtn) {
            nextBtn.addEventListener('click', function () {
                show(index + 1);
                start();
            });
        }
        if (prevBtn) {
            prevBtn.addEventListener('click', function () {
                show(index - 1);
                start();
            });
        }
        dots.forEach(function (dot, i) {
            dot.addEventListener('click', function () {
                show(i);
                start();
            });
        });

        // Dừng tự chạy khi con trỏ nằm trên slider
        root.addEventListener('mouseenter', function () { paused = true; });
        root.addEventListener('mouseleave', function () { paused = false; });
        root.addEventListener('focusin', function () { paused = true; });
        root.addEventListener('focusout', function () { paused = false; });

        // Điều khiển bằng bàn phím
        root.addEventListener('keydown', function (event) {
            if (event.key === 'ArrowLeft') {
                show(index - 1);
                start();
            } else if (event.key === 'ArrowRight') {
                show(index + 1);
                start();
            }
        });

        // Vuốt trên màn hình cảm ứng
        let touchX = null;
        root.addEventListener('touchstart', function (event) {
            touchX = event.touches[0].clientX;
        }, { passive: true });
        root.addEventListener('touchend', function (event) {
            if (touchX === null) {
                return;
            }
            const delta = event.changedTouches[0].clientX - touchX;
            if (Math.abs(delta) > 40) {
                show(index + (delta < 0 ? 1 : -1));
                start();
            }
            touchX = null;
        }, { passive: true });

        show(0);
        if (slides.length > 1) {
            start();
        }
    });
})();
