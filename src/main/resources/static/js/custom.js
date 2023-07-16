    //tooltip js from bootstrap instruction
    $(function () {
        $('[data-toggle="tooltip"]').tooltip();
    });

    //function for changing the value of the form action
    function setFormAction(action, index) {
        var forms = document.getElementsByClassName('change_action_form');
        var form = forms[index];
        form.action = action;
    };

    //function for swapping the visibility of the mentioned ids
    function swapContent(...ids) {
        for (let id of ids) {
            var content = document.getElementById(id);
            if (content.style.display === 'none') {
                content.style.display = '';
            } else {
                content.style.display = 'none';
            }
        }
    };