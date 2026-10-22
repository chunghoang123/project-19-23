/* =============================================================================
   CineX - Xem trailer phim (modal phát video YouTube nhúng trong trang)
   ============================================================================= */
(function () {
    'use strict';

    /**
     * Chuyển URL YouTube bất kỳ sang URL nhúng.
     * Hỗ trợ: watch?v=, youtu.be/, /embed/, /shorts/.
     */
    function toEmbedUrl(url) {
        if (!url) {
            return null;
        }
        var id = null;
        try {
            var parsed = new URL(url, window.location.origin);
            var host = parsed.hostname.replace(/^www\./, '');

            if (host === 'youtu.be') {
                id = parsed.pathname.substring(1);
            } else if (host === 'youtube.com' || host === 'm.youtube.com'
                || host === 'music.youtube.com' || host === 'youtube-nocookie.com') {
                if (parsed.pathname === '/watch') {
                    id = parsed.searchParams.get('v');
                } else {
                    var match = parsed.pathname.match(/^\/(embed|shorts|v)\/([^/?]+)/);
                    if (match) {
                        id = match[2];
                    }
                }
            }
        } catch (e) {
            return null;
        }

        if (!id) {
            return null;
        }
        return 'https://www.youtube-nocookie.com/embed/' + id
            + '?autoplay=1&rel=0&modestbranding=1';
    }

    var modal = document.querySelector('[data-trailer-modal]');
    if (!modal) {
        return;
    }
    var frame = modal.querySelector('[data-trailer-frame]');
    var caption = modal.querySelector('[data-trailer-caption]');
    var lastFocused = null;

    function open(url, title) {
        var embed = toEmbedUrl(url);
        if (!embed) {
            return;
        }
        lastFocused = document.activeElement;
        if (caption) {
            caption.textContent = title || 'Trailer phim';
        }
        frame.innerHTML = '<iframe src="' + embed + '" title="Trailer phim" allow="autoplay; encrypted-media; picture-in-picture" allowfullscreen></iframe>';
        modal.hidden = false;
        document.body.style.overflow = 'hidden';
        modal.querySelector('[data-trailer-close]').focus();
    }

    function close() {
        modal.hidden = true;
        frame.innerHTML = '';
        document.body.style.overflow = '';
        if (lastFocused) {
            lastFocused.focus();
        }
    }

    document.addEventListener('click', function (event) {
        var trigger = event.target.closest('[data-trailer-open]');
        if (trigger) {
            event.preventDefault();
            open(trigger.dataset.trailerUrl, trigger.dataset.trailerTitle);
            return;
        }
        if (event.target.closest('[data-trailer-close]')) {
            event.preventDefault();
            close();
        }
    });

    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape' && !modal.hidden) {
            close();
        }
    });
})();
