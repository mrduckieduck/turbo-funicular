(function($){
    $(function() {
        const gistModal = $('.modal');
        gistModal.modal({
            onOpenEnd: function(modal, trigger) {
                const gistName = $(trigger).attr('data-description');
                $(modal).find('#gistName').text(gistName);
                console.log('Gist name: ', gistName);
            }
        });
    });
})(jQuery);
