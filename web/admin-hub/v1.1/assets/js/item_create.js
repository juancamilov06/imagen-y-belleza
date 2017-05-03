$(document).ready(function(){
    getSession();
    getData();
});

function checkCode(code){
    $.ajax({
        url: 'http://' + ip + '/admin-hub/v1.1/items/find/' + code,
        type: 'get',
        beforeSend: function () {
            $.showLoading({
                name: 'square-flip'
            });
        },
        error: function (jqXHR, exception) {
            $.hideLoading();
            createProduct();
        },
        success: function (response) {
            $.hideLoading();
            var item = JSON.parse(response);
            alert('El codigo ingresado pertenece al producto: ' + item.name);
        }
    });
}

$(document).keypress(function (e) {
    if (e.which == 13) {
        sendData();
    }
});

function createProduct(){

    var id = $('#id').val();
    var name = $('#name').val();
    var minimum = $('#minimum').val();
    var iva = $('#iva').val();
    var brand_id = $('#brands').val();
    var category_id = $('#categories').val();

    var payment_one = $('#pay-1').val();
    var payment_two = $('#pay-2').val();
    var payment_three = $('#pay-3').val();
    var payment_four = $('#pay-4').val();

    var client_one = $('#dis-1').val();
    var client_two = $('#dis-2').val();
    var client_three = $('#dis-3').val();
    var client_four = $('#dis-4').val();
    var client_five = $('#dis-5').val();

    if (empty(name) || empty(minimum) || empty(iva) || empty(client_one) || empty(client_two) || empty(client_three) || empty(client_four)
        || empty(client_five) || minimum < 0){

        alert('Error, verifique los datos');

    } else {

        var item = {
            'id':id,
            'name':name,
            'minimum_units':minimum,
            'iva':iva,
            'payment_one':payment_one,
            'payment_two':payment_two,
            'payment_three':payment_three,
            'payment_four':payment_four,
            'price_one':client_one,
            'price_two':client_two,
            'price_three':client_three,
            'price_four':client_four,
            'price_five':client_five,
            'is_active': 1,
            'is_new':1,
            'brand_id': brand_id,
            'category_id': category_id
        };

        var itemObject = JSON.stringify(item);
        var params = {
            'item':itemObject
        };

        $.ajax({
            url:   'http://' + ip + '/admin-hub/v1.1/items/new',
            data: params,
            type:  'post',
            beforeSend: function(){
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
                console.log(response);
                if (response == 'Creado'){
                    alert('Creado con exito');
                    location.reload();
                } else {
                    alert('Error creando el producto');
                }
            }
        });
    }
}

function getData(){
    $.ajax({
        url:   'http://' + ip + '/admin-hub/v1.1/categories/all',
        type:  'get',
        beforeSend: function(){
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
            var categories = JSON.parse(response);
            $.each(categories, function(key,value) {
                $('#categories').append('<option value=' + value.id + '>' + value.name + '</option>');
            });
        }
    });

    $.ajax({
        url:   'http://' + ip + '/admin-hub/v1.1/brands/all',
        type:  'get',
        beforeSend: function(){
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
            var brands = JSON.parse(response);
            $.each(brands, function(key,value) {
                $('#brands').append('<option value=' + value.id + '>' + value.name + '</option>');
            });
        }
    });
}

function sendData(){
    var id = $('#id').val();
    if (empty(id)){
        alert('Debe ingresar el codigo del producto');
    } else {
        checkCode(id);
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
        window.location.replace("/admin-hub/v1.1/main");
    };
}