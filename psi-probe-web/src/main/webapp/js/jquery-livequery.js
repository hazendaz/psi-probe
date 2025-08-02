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
(function ($) {

    $.extend($.fn, {
        livequery: function (type, fn, fn2) {
            var self = this,
                q;

            // Handle different call patterns
            if ($.isFunction(type))
                fn2 = fn, fn = type, type = undefined;

            // See if Live Query already exists
            for (var i = 0, l = $.livequery.queries.length; i < l; i++) {
                var query = $.livequery.queries[i];
                if (self.selector == query.selector && self.context == query.context &&
                    type == query.type && (!fn || fn.$lqguid == query.fn.$lqguid) && (!fn2 || fn2.$lqguid == query.fn2.$lqguid)) {
                // Found the query, exit the each loop
                    if (q = query) break;
                }
            }

            // Create new Live Query if it wasn't found
            q = q || new $.livequery(this.selector, this.context, type, fn, fn2);

            // Make sure it is running
            q.stopped = false;

            // Run it immediately for the first time
            q.run();

            // Contnue the chain
            return this;
        },

        expire: function (type, fn, fn2) {
            var self = this;

            // Handle different call patterns
            if ($.isFunction(type))
                fn2 = fn, fn = type, type = undefined;

            // Find the Live Query based on arguments and stop it
            for (var i = 0, l = $.livequery.queries.length; i < l; i++) {
                var query = $.livequery.queries[i];
                if (self.selector == query.selector && self.context == query.context &&
                    (!type || type == query.type) && (!fn || fn.$lqguid == query.fn.$lqguid) && (!fn2 || fn2.$lqguid == query.fn2.$lqguid) && !this.stopped) {
                    $.livequery.stop(query.id);
                }
            }

            // Continue the chain
            return this;
        }
    });

    $.livequery = function (selector, context, type, fn, fn2) {
        this.selector = selector;
        this.context = context;
        this.type = type;
        this.fn = fn;
        this.fn2 = fn2;
        this.elements = $();
        this.stopped = false;

        // The id is the index of the Live Query in $.livequery.queries
        this.id = $.livequery.queries.push(this) - 1;

        // Mark the functions for matching later on
        if (fn) fn.$lqguid = fn.$lqguid || $.livequery.guid++;
        if (fn2) fn2.$lqguid = fn2.$lqguid || $.livequery.guid++;

        // Return the Live Query
        return this;
    };

    $.livequery.prototype = {
        stop: function () {
            var query = this;

            if (this.type) {
                // Unbind all bound events
                this.elements.off(this.type, this.fn);
            } else if (this.fn2) {
                // Call the second function for all matched elements
                for (var i = 0, l = this.elements.length; i < l; i++) {
                    var el = this.elements[i];
                    query.fn2.apply(el);
                }
            }

            // Clear out matched elements
            this.elements = $();

            // Stop the Live Query from running until restarted
            this.stopped = true;
        },

        run: function () {
            // Short-circuit if stopped
            if (this.stopped) return;
            var query = this;

            var oEls = this.elements,
                els = $(this.selector, this.context),
                nEls = els.not(oEls);

            // Set elements to the latest set of matched elements
            this.elements = els;

            if (this.type) {
                // Bind events to newly matched elements
                nEls.on(this.type, this.fn);

                // Unbind events to elements no longer matched
                if (oEls.length > 0) {
                    for (var i = 0, l = oEls.length; i < l; i++) {
                        var el = oEls[i];
                        if ($.inArray(el, els) === -1)
                            $.event.remove(el, query.type, query.fn);
                    }
                }
            } else {
                // Call the first function for newly matched elements
                if (this.fn && nEls.length > 0) {
                    for (var i = 0, l = nEls.length; i < l; i++) {
                        var el = nEls[i];
                        query.fn.apply(el);
                    }
                }

                // Call the second function for elements no longer matched
                if (this.fn2 && oEls.length > 0) {
                    for (var i = 0, l = oEls.length; i < l; i++) {
                        var el = oEls[i];
                        if ($.inArray(el, els) === -1)
                            query.fn2.apply(el);
                    }
                }
            }
        }
    };

    $.extend($.livequery, {
        guid: 0,
        queries: [],
        queue: [],
        running: false,
        timeout: null,
        registered: [],

        checkQueue: function () {
            var length = $.livequery.queue.length;
            if ($.livequery.running && length) {
                // Run each Live Query currently in the queue
                while (length--)
                    $.livequery.queries[$.livequery.queue.shift()].run();
            }
        },

        pause: function () {
            // Don't run anymore Live Queries until restarted
            $.livequery.running = false;
        },

        play: function () {
            // Restart Live Queries
            $.livequery.running = true;
            // Request a run of the Live Queries
            $.livequery.run();
        },

        registerPlugin: function () {
            for (var i = 0, l = arguments.length; i < l; i++) {
                (function (n) {
                    // Short-circuit if the method doesn't exist
                    if (!$.fn[n] || $.livequery.registered.indexOf(n) > -1) return;

                    // Save a reference to the original method
                    var old = $.fn[n];

                    // Create a new method
                    $.fn[n] = function () {

                        // Call the original method
                        var r = old.apply(this, arguments);

                        // Request a run of the Live Queries
                        $.livequery.run();

                        // Return the original methods result
                        return r;
                    }

                    $.livequery.registered.push(n);
                })(arguments[i]);
            }
        },

        run: function (id) {
            if (id != undefined) {
                // Put the particular Live Query in the queue if it doesn't already exist
                if ($.livequery.queue.indexOf(id) === -1)
                    $.livequery.queue.push(id);
            } else {
                // Put each Live Query in the queue if it doesn't already exist
                for (var i = 0, l = $.livequery.queries.length; i < l; i++) {
                    if ($.livequery.queue.indexOf(i) === -1)
                        $.livequery.queue.push(i);
                }
            }

            // Clear timeout if it already exists
            if ($.livequery.timeout) clearTimeout($.livequery.timeout);
            // Create a timeout to check the queue and actually run the Live Queries
            $.livequery.timeout = setTimeout($.livequery.checkQueue, 50);
        },

        stop: function (id) {
            if (id != undefined) {
                // Stop are particular Live Query
                $.livequery.queries[id].stop();
            } else {
                // Stop all Live Queries
                for (var i = 0, l = $.livequery.queries.length; i < l; i++) {
                    $.livequery.queries[i].stop();
                }
            }
        }
    });

    $.livequery.registerPlugin('append', 'prepend', 'after', 'before', 'wrap', 'attr', 'removeAttr', 'prop', 'removeProp', 'addClass', 'removeClass', 'toggleClass', 'empty', 'remove', 'html');

    // Run Live Queries when the Document is ready
    $(function () {
        $.livequery.play();
    });

})(jQuery);
