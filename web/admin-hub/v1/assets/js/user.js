var inst = $('[data-remodal-id=modal]').remodal();

$(document).ready(function(){
    $.showLoading({
        name: 'square-flip'
    });
    $("#table").bootstrapTable({
        url:'http://' + ip + '/admin-hub/v1/users/all',
        columns:[
            {title:"Identificador", field:"id"},
            {title:"Nombre de usuario", field:"username"},
            {title:"Empleado", field:"contact"},
            {title:"Activo", field:"is_active"},
            {title:"Rol", field:"role"}
        ],
        onClickRow: function (row, $element) {
            getData(row.id);
            inst.open();
        },
        onLoadSuccess: function (data) {
            $.hideLoading();
            toggle();
            getSession();
        }
    });
});

$(document).on('confirmation', '.remodal', function () {
    updateUser();
});

function getData(userId){
    var params = {
        "id" : userId
    };
    $.ajax({
        data:  params,
        url:   'http://' + ip + '/admin-hub/v1/users/find/' + userId,
        type:  'get',
        beforeSend: function () {
            $.showLoading({
                name: 'square-flip'
            });
        },
        error: function (jqXHR, exception) {
            $.hideLoading();
            alert('Error, intente de nuevo');
        },
        success:  function (response) {
            $.hideLoading();
            var user = $.parseJSON(response);
            $('#id').val(user.id);
            $('#username').val(user.username);
            $('#contact').val(user.contact);
            var role = user.role;
            if (role == "Admin") {
                $("#roles").val("1").change();
            } else if (role == "Facturacion"){
                $("#roles").val("2").change();
            } else {
                $("#roles").val("3").change();
            }
            var active = user.is_active;
            if (active == 1) {
                $("#active").val("1").change();
            } else {
                $("#active").val("2").change();
            }
            Materialize.updateTextFields();
        }
    });
}

function empty(value){
    return (value == null || value === "" || !/\S/.test(value));
}

function updateUser() {

    var id = $('#id').val();
    var username = $('#username').val();
    var contact = $('#contact').val();
    var is_active;
    if ($("#active option:selected").text() == "Activo") {
        is_active = 1;
    } else {
        is_active = 0;
    }
    var role = $("#roles option:selected").text()
    var user = {
        'id': id,
        'contact': contact,
        'username': username,
        'is_active': is_active,
        'role': role
    };
    
    if(empty(username) || empty(contact)){
        alert('Debe llenar todos los campos')
    } else {
        var userParam = JSON.stringify(user);
        var params = {
            "user": userParam
        };
        $.ajax({
            data: params,
            url: 'http://' + ip + '/admin-hub/v1/users/update',
            type: 'post',
            beforeSend: function () {
                $.showLoading({
                    name: 'square-flip'
                });
            },
            error: function (jqXHR, exception) {
                $.hideLoading();
                alert('Error, intente de nuevo');
            },
            success: function (response) {
                $.hideLoading();
                if (response.trim() == 'Actualizado') {
                    alert("Actualizado con exito");
                    location.reload();
                } else {
                    alert("Error en la actualizacion");
                }
            }
        });        
    }
}

function getSession(){
    if (localStorage.getItem("role") == null) {
        window.location.replace("/admin-hub/v1/");
    }
    if (localStorage.getItem("role") != 'Admin') {
        alert("Solo el administrador tiene acceso");
        window.location.replace("main");
    }
}