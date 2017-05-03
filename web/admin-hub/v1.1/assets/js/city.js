var preVal = '';

$(document).ready(function(){
    $("#table").bootstrapTable({
        url:'http://' + ip + '/dashboard/web-services/cityService.php',
        columns:[
        {title:"Identificador", field:"id"},
        {title:"Ciudad", field:"city", editable: true}

        ],
        onEditableSave: function(field, row, $el, reason){
            if (row.city.trim() && row.city != preVal && row.city) {
                params = {
                    "id": row.id,
                    "name": row.city
                }
                $.ajax({
                    data:  params,
                    url:   'http://' + ip + '/dashboard/web-services/cityService.php',
                    type:  'post',
                    success:  function (response) {
                        if (response.trim() === 'true') {
                            alert("Actualizado correctamente");
                        } else {
                            alert("No se pudo actualizar");
                            $("#table").bootstrapTable('updateRow', {index: $el.attr('data-index'), row: {
                                id:row.id,
                                city: preVal
                            }});
                        }
                    }
                });
            } else {
                $("#table").bootstrapTable('updateRow', {index: $el.attr('data-index'), row: {
                    id:row.id,
                    city: preVal
                }});
            };
        }, onEditableShown: function(editable, field, row, $el){
            preVal = $el.value;
        }
    });
    getSession();
});

function getSession(){
    if (localStorage.getItem("role") == null) {
        window.location.replace("/admin-hub/");
    }
    if (localStorage.getItem("role") != 'Admin') {
        alert("Solo el administrador tiene acceso");
        window.location.replace("main");
    };
}