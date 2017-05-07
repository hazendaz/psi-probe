/*
 * Licensed under the GPL License. You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * THIS PACKAGE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF MERCHANTIBILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE.
 */
function inverse($f) {
    for (const element of $f.elements) {
        if (element.type == "checkbox") {
            element.checked = !element.checked;
        }
    }
    return false;
}

function checkAll($f) {
    for (const element of $f.elements) {
        if (element.type == "checkbox") {
            element.checked = true;
        }
    }
    return false;
}

/**
 * Requires prototype.js (http://www.prototypejs.org/)
 */
$.ajax.ImgUpdater = Class.create();
$.ajax.ImgUpdater.prototype = {
    initialize: function(imgID, timeout, newSrc) {
        this.img = document.getElementById(imgID);
        if (newSrc) {
            this.src = newSrc;
        } else {
            this.src = this.img.src;
        }
        this.timeout = timeout;
        this.start();
    },

    start: function() {
        let now = new Date();
        this.img.src = this.src + '&t=' + now.getTime();
        this.timer = setTimeout(this.start.bind(this), this.timeout * 1000);
    },

    stop: function() {
        if (this.timer) {
            clearTimeout(this.timer);
        }
    }
}

function togglePanel(container, remember_url) {
    if (Element.css(container, "display") == 'none') {
        if (remember_url) {
            new $.ajax.Request(remember_url, {
                method:'get',
                asynchronous:true,
                parameters: 'state=on'
            });
        }
        if (document.getElementById('invisible_' + container)) {
            Element.hide('invisible_' + container);
        }
        if (document.getElementById('visible_' + container)) {
            Element.show('visible_' + container);
        }

        Effect.Grow(container);
    } else {
        if (remember_url) {
            new $.ajax.Request(remember_url, {
                method:'get',
                asynchronous:true,
                parameters: 'state=off'
            });
        }
        if (document.getElementById('visible_' + container)) {
            Element.hide('visible_' + container);
        }
        if (document.getElementById('invisible_' + container)) {
            Element.show('invisible_' + container);
        }
        Effect.Shrink(container);
    }
    return false;
}

function scaleImage(v, minX, maxX, minY, maxY) {
    let images = document.getElementsByClassName('scale-image');
    let w = (maxX - minX) * v + minX;
    let h = (maxY - minY) * v + minY;
    for (const element of images) {
        $(element).css({
            "width": w + 'px',
            "height": h + 'px'
        });
    }
}

function toggleAndReloadPanel(container, url) {
    if (Element.css(container, "display") == 'none') {
        new $.ajax.Updater(container, url);
        Effect.BlindDown(container);
    } else {
        Effect.Shrink(container);
    }
}

function getWindowHeight() {
    let myHeight = 0;
    if (typeof( window.innerHeight ) == 'number') {
        //Non-IE
        myHeight = window.innerHeight;
    } else if (document.documentElement && document.documentElement.clientHeight) {
        //IE 6+ in 'standards compliant mode'
        myHeight = document.documentElement.clientHeight;
    }
    return myHeight;
}

function getWindowWidth() {
    let myWidth = 0;
    if (typeof( window.innerWidth ) == 'number') {
        //Non-IE
        myWidth = window.innerWidth;
    } else if (document.documentElement && document.documentElement.clientWidth) {
        //IE 6+ in 'standards compliant mode'
        myWidth = document.documentElement.clientWidth;
    }
    return myWidth;
}

let helpTimerID;

function setupHelpToggle(url) {
    let rules = {
        'li#abbreviations': function(element) {
            element.onclick = function() {
                let help_container = 'help';
                if (Element.css(help_container, "display") == 'none') {
                    new $.ajax.Updater(help_container, url);
                }
                Effect.toggle(help_container, 'appear');
                if (helpTimerID) {
                    clearTimeout(helpTimerID);
                }
                helpTimerID = setTimeout('Effect.Fade("' + help_container + '")', 15000);
                return false;
            }
        }
    }
    jQuery.behavior(rules)
    jQuery(document).ready();
}

function addAjaxTooltip(activator, tooltip, url) {
    Tooltip.closeText = null;
    Tooltip.autoHideTimeout = null;
    Tooltip.showMethod = function(e) {
        Effect.Appear(e, {
            to: 0.9
        });
    }

    Tooltip.add(activator, tooltip);
    let tt_container = $('#' + tooltip + ' .tt_content')[0];
    Event.bind(activator, 'click', function(e) {

        let t_title = $('#tt_title');

        if (t_title) {
            t_title.hide();
        }

        tt_container.style.width = '300px';

        tt_container.innerHTML = '<div class="ajax_activity">&nbsp;</div>';
        new $.ajax.Updater(tt_container, url, {
            method: 'get',
            onComplete: function() {
                tt_container.style.width = null;
                let the_title = $('#tooltip_title');
                t_title = $('#tt_title');

                if (the_title && t_title) {
                    the_title.hide();
                    t_title.innerHTML = the_title.innerHTML;
                    t_title.show();
                }
            }
        });
    });
}
