<?php
    $orderId = $_GET['order_id'];
?>

<!DOCTYPE html>
<html lang="en">

<head>

    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, shrink-to-fit=no, initial-scale=1">
    <meta name="description" content="">
    <meta name="author" content="">

    <title>Administración</title>

    <link href="<?php echo base_url('assets/css/bootstrap.min.css'); ?>" rel="stylesheet">
    <link rel="stylesheet" type="text/css" href="<?php echo base_url('assets/css/font-awesome.min.css'); ?>">
    <link href="<?php echo base_url('assets/css/simple-sidebar.css'); ?>" rel="stylesheet">
    <link rel="stylesheet" type="text/css" href="<?php echo base_url('assets/css/main.css'); ?>">
    <link rel="stylesheet" type="text/css" href="<?php echo base_url('assets/css/loading.min.css'); ?>">
    <link rel="stylesheet" type="text/css" href="<?php echo base_url('assets/css/materialize.min.css'); ?>">

</head>

<body>
    <div class="container-fluid">            
        <div class="container table-container">
            <div class="page-header text-center">
                <div class = "row">
                    <button type="button" id="back-btn" class="btn btn-default" aria-label="Left Align">
                      <span class="glyphicon glyphicon-chevron-left" aria-hidden="true"></span> Volver
                    </button>
                    <h2>Detalle del pedido N°: <?php echo $_GET['order_id']; ?></h2>
                </div>
                <h6 id="client"></h6>
                <h6 id="phones"></h6>
                <h6 id="address-neighborhood"></h6>
                <h6 id="seller"></h6>
                <button style="margin-top: 5%" class="btn btn-default" id="print-btn" onclick="sendDataAndPrint()">Imprimir</button>
            </div>
            <div class="row">
                <div class="col-md-12 col-lg-12 col-sm-12 col-xs-12">
                    <table id="table"></table>
                </div>
            </div>
            <div class="row" style="margin-top: 5%">
                <div "col-md-12 col-lg-12 col-sm-12 col-xs-12">
                    <div class="row text-center">
                        <h6 id="subtotal"></h6>
                    </div>
                    <div class="row text-center">
                        <h6 id="iva"></h6>
                    </div>
                    <div class="row text-center">
                        <h6 id="discount"></h6>
                    </div>
                    <div class="row text-center">
                        <h6 id="total"></h6>
                    </div>
                </div>
            </div>
            <div class="row">
                <div "col-md-12 col-lg-12 col-sm-12 col-xs-12">
                    <div class="row text-center">
                        <h5>Información adicional del pedido</h5>
                    </div>
                    <div class="row text-center">
                        <h6 id="client-type"></h6>
                    </div>
                    <div class="row text-center">
                        <h6 id="notes"></h6>
                    </div>
                    <div class="row text-center">
                        <h6 id="payment-notes"></h6>
                    </div>
                    <div class="row text-center">
                        <h6 id="payment"></h6>
                    </div>
                    <div class="row text-center">
                        <h6 id="deliver"></h6>
                    </div>
                </div>
            </div>
        </div>           <!-- /#page-content-wrapper -->

    </div>
<!-- /#wrapper -->
    <script type="text/javascript" src="<?php echo base_url('assets/js/constants.js'); ?>"></script>
    <script src="<?php echo base_url('assets/js/jquery.js'); ?>"></script>
    <script src="<?php echo base_url('assets/js/bootstrap.min.js'); ?>"></script>
    <script type="text/javascript" src="<?php echo base_url('assets/js/jquery.loading.min.js'); ?>"></script>
<link rel="stylesheet" href="//cdnjs.cloudflare.com/ajax/libs/bootstrap-table/1.11.0/bootstrap-table.min.css">

<!-- Latest compiled and minified JavaScript -->
<script src="//cdnjs.cloudflare.com/ajax/libs/bootstrap-table/1.11.0/bootstrap-table.min.js"></script>

<!-- Latest compiled and minified Locales -->
<script src="//cdnjs.cloudflare.com/ajax/libs/bootstrap-table/1.11.0/locale/bootstrap-table-es-ES.min.js"></script>

<script type="text/javascript">
    var id = <?php echo $_GET['order_id']; ?>;
</script>

<script type="text/javascript" src="<?php echo base_url('assets/js/detail.js'); ?>"></script>
<script type="text/javascript">
            $.ajax({
                url:   'http://' + ip + '/admin-hub/orders/' + <?php echo $_GET['order_id']; ?> + '/detail',
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
                    var json = $.parseJSON(response);
                    $("#seller").text("Vendedor: " + json.seller_id);
                    $("#payment").text("Tipo de pago: " + json.payment_id);
                    $("#client").text("Cliente: " + json.client_name + " Empresa: " + json.company);
                    $("#phones").text("Telefono(s): " + json.phone_one + " ");
                    $("#address-neighborhood").text("Direccion: " + json.address + " Barrio: " + json.neighborhood);
                    $("#client-type").text("Tipo de cliente: " + json.type);
                    if (json.notes != "" && json.notes != "null") {
                       $("#notes").text("Notas del pedido: " + json.notes);
                    } else {
                        $("#notes").hide();
                    }
                    if (json.payment_notes != "" && json.payment_notes != "null") {
                       $("#payment-notes").text("Notas de pronto pago: " + json.payment_notes);
                   } else {
                       $("#payment-notes").hide();
                   }
                    $("#deliver").text("Fecha de entrega: " + json.deliver);                
            }
        });
</script>

<script type="text/javascript">
$.showLoading({
     name: 'square-flip'
});

var order_id = <?php echo $_GET['order_id']; ?>;

$('#table').bootstrapTable({
        url: 'http://'+ ip +'/admin-hub/orders/'+order_id+'/items',
        columns: [{
            field: 'subitem_name',
            title: 'Nombre'
        },{
            field: 'items',
            title: 'Subproductos'
        }, {
            field: 'unit_price',
            title: 'Precio unitario'
        }, {
            field: 'units',
            title: 'Unidades'
        },{
            field: 'free_units',
            title: 'Unidades de regalo'
        },{
            field: 'total',
            title: 'Total'
        },{
            field: 'discount',
            title: 'Descuento'
        },{
            field: 'iva',
            title: 'IVA'
        }
        ],
        onLoadSuccess: function (data) {
            $.hideLoading();
            var total = 0;
            var subTotal = 0;
            var discount = 0;
            var iva = 0;
            var rows = document.getElementsByTagName("tr");
            $.each(data, function(i, item) {
                if (item.subitem_id === '0') {
                    discount = discount + ((item.unit_price - item.value) * item.units);
                    var bg = parseFloat(item.total) / (1 + (parseFloat(item.iva) / 100));
                    subTotal = subTotal + bg;
                };
                if (item.subitem_id === '0') {
                    rows[i+1].style.backgroundColor = "#FFFFFF";
                };
                total = total + parseFloat(item.total);
            });
            subTotal = subTotal.toFixed(2);
            iva = (total - subTotal).toFixed(2); 
            $("#storage_units").text();
            $("#subtotal").text("Subtotal: " + Math.round(subTotal).toLocaleString());
            $("#iva").text("IVA: " + Math.round(iva).toLocaleString());
            $("#total").text("Total del pedido: " + Math.round(total).toLocaleString());
            $("#discount").text("Descuento total: " + Math.round(discount).toLocaleString());
        },
        onLoadError: function(error){
            $.hideLoading();
        }
});
</script>

</body>

</html>