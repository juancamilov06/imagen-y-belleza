var preVal = '';
var inst = $('[data-remodal-id=modal]').remodal();

$(document).ready(function(){
    $.showLoading({
        name: 'square-flip'
    });
    $("#table").bootstrapTable({
        url:'http://' + ip + '/admin-hub/v1.1/items/all',
        search: true,
        pagination: true,
        columns:[
            {title:"Codigo", field:"id"},
            {title:"Nombre", field:"name"},
            {title:"Unidades disponibles", field:"available_units"},
            {title:"Minimo disponibles", field:"minimum_available"},
            {title:"Precio", field:"price"}
        ],
        onClickRow: function (row, $element) {
            getData(row.id);
            inst.open();
        },
        onLoadSuccess: function (data) {
            $.hideLoading();
            toggle();
            getSession();
        }, OnLoadError: function (error) {
            $.hideLoading();
        }
    });
});

$(document).on('confirmation', '.remodal', function () {
    updateItem();
});

function getData(itemId){
    $.ajax({
        url:   'http://' + ip + '/admin-hub/v1.1/items/find/' + itemId,
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
            var item = $.parseJSON(response);
            $('#id').val(item.id);
            $('#name').val(item.name);
            $('#price').val(item.price);
            $('#iva').val(item.iva);
            $('#minimum').val(item.minimum_available);
            $('#pay-1').val(item.payment_one);
            $('#pay-2').val(item.payment_two);
            $('#pay-3').val(item.payment_three);
            $('#pay-4').val(item.payment_four);
            var active = item.is_active;
            if (active == "1") {
                $("#active").val("1").change();
            } else if (active == "0"){
                $("#active").val("0").change();
            }
            Materialize.updateTextFields();
        }
    });
}

function updateItem(){

    var id = $('#id').val();
    var price = $('#price').val();
    var iva = $('#iva').val();
    var name = $('#name').val();
    var is_active = 1;
    var minimum = $('#minimum').val();
    if ($("#active option:selected").text() == "Activo") {
        is_active = '1';
    } else {
        is_active = '0';
    }
    var payment_one = $('#pay-1').val();
    var payment_two = $('#pay-2').val();
    var payment_three = $('#pay-3').val();
    var payment_four = $('#pay-4').val();

    if (empty(price) || empty(name) || empty(iva) || empty(payment_one) || empty(payment_two) || empty(payment_three) || empty(payment_four) || empty(minimum)){
        alert('Debes llenar todos los campos');
    } else {
        var item = {
            'id' : id,
            'price' : price,
            'iva': iva,
            'name':name,
            'is_active':is_active,
            'payment_one':payment_one,
            'payment_two':payment_two,
            'payment_three':payment_three,
            'payment_four':payment_four
        };
        var itemObject = JSON.stringify(item);
        var params = {
            'item':itemObject.replace("/", "")
        };

        $.ajax({
            data: params,
            url: 'http://' + ip + '/admin-hub/v1.1/items/update',
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
                console.log(response);
                $.hideLoading();
                if (response.trim() == 'true') {
                    alert("Actualizado con exito");
                    location.reload();
                } else {
                    alert("Error en la actualizacion");
                }
            }
        });
    }
}

function empty(value){
    return (value == null || value === "" || !/\S/.test(value));
}

function getSession(){
    if (localStorage.getItem("role") == null) {
        window.location.replace("/admin-hub/v1.1/");
    }
    if (localStorage.getItem("role") != 'Admin') {
        alert("Solo el administrador tiene acceso");
        window.location.replace("main");
    };
}