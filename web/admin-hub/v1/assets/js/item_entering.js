$(document).ready(function(){
    $.ajax({
            url:   'http://' + ip + '/admin-hub/v1/items/all',
            type:  'get',
            beforeSend: function() { 
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
                var items = JSON.parse(response);
                $.each(items, function(key,value) {
                  $('#items').append('<option value=' + value.id + '>' + value.name + '</option>');
                }); 
            }
        });

    $.ajax({
            url:   'http://' + ip + '/dashboard/web-services/providerService.php',
            type:  'get',
            beforeSend: function() { 
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
                var items = JSON.parse(response);
                $.each(items, function(key,value) {
                  $('#providers').append('<option value=' + value.id + '>' + value.name + '</option>');
                }); 
            }
        });
    getSession();
});

function sendData(){

    var units = $('#units').val();
    var discount = $('#discount').val();
    var unitPrice = $('#price').val();
    var iva = $('#iva').val();
    var itemId = $('#items').val();
    var providerId = $('#providers').val();
    var notes = $('#notes').val();

    if (checkValids(units, discount, unitPrice, iva)) {

        var entering = {
            'units' : units,
            'discount' : discount,
            'price' : unitPrice,
            'iva' : iva,
            'item_id': itemId,
            'provider_id':providerId,
            'notes': notes
        };

        var params = {
            'entering' : JSON.stringify(entering)
        };
        
        $.ajax({
            url:   'http://' + ip + '/admin-hub/v1/entering/create',
            data: params,
            type:  'post',
            beforeSend: function() { 
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
                if (response == 'Creado') {
                    $('#units').val('');
                    $('#discount').val('');
                    $('#price').val('');
                    $('#iva').val('');
                    $('#notes').val('');
                    alert('Ingreso creado con exito');
                } else {
                    alert("Error creando el ingreso");
                }
            }
        });
    } else {
        alert('Debes agregar todos los campos');
    };
}

$(document).keypress(function (e) {
  if (e.which == 13) {
    sendData();
  }
});

function checkValids(units, discount, unitPrice, iva){
    return $.isNumeric(units) && $.isNumeric(discount) && $.isNumeric(unitPrice) && $.isNumeric(iva);
}

function getSession(){
    if (localStorage.getItem("role") == null) {
        window.location.replace("/admin-hub/v1/");
    }
    if (localStorage.getItem("role") != 'Admin') {
        alert("Solo el administrador tiene acceso");
        window.location.replace("main");
    };
}