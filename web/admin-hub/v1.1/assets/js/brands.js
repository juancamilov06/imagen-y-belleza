var inst = $('[data-remodal-id=modal]').remodal();

$(document).ready(function(){
    $("#table").bootstrapTable({
        url:'http://' + ip + '/admin-hub/v1.1/brands/all',
        columns:[
            {title:"Identificador", field:"id"},
            {title:"Marca", field:"name"}
        ],
        onClickRow: function (row, $element) {
            getData(row.id, row.name);
            inst.open();
        }
    });
    getSession();
});

function empty(value){
    return (value == null || value === "" || !/\S/.test(value));
}

function updateBrand() {

    var id  = $('#id').val();
    var name = $('#name').val();

    if (empty(id) || empty(name)){
        alert('Debes completar todos los datos');
    } else {

        params = {
            "id": id,
            "name": name
        };

        $.ajax({
            data:  params,
            url:   'http://' + ip + '/admin-hub/v1.1/brands/update',
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
                if (response.trim() === 'true') {
                    alert("Actualizado correctamente");
                    location.reload();
                } else {
                    alert("No se pudo actualizar");
                }
            }
        });
    }
}
$(document).on('confirmation', '.remodal', function () {
    updateBrand();
});

function getData(brandId, name){
    $('#id').val(brandId);
    $('#name').val(name);
    Materialize.updateTextFields();
}

function getSession(){
    if (localStorage.getItem("role") == null) {
        replace("/admin-hub/v1.1/");
    }
    if (localStorage.getItem("role") != 'Admin') {
        alert("Solo el administrador tiene acceso");
        window.location.replace("main");
    };
}