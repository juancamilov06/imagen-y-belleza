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

<div id="wrapper">
    <!-- Sidebar -->
    <div id="sidebar-wrapper">
        <ul class="sidebar-nav">
            <li class="sidebar-brand">
                Administración
            </li>
            <li>
                <a href="/admin-hub/v1.1/main">
                    Pedidos
                </a>
            </li>
            <li>
                <a href="/admin-hub/v1.1/clients">
                    Clientes
                </a>
            </li>
            <li>
                <a href="/admin-hub/v1.1/brands">
                    Marcas
                </a>
            </li>
            <li>
                <a href="/admin-hub/v1.1/categories">
                    Categorias
                </a>
            </li>
            <li>
                <a href="/admin-hub/v1.1/inventory">
                    Inventario
                </a>
            </li>
            <li>
                <a href="/admin-hub/v1.1/users">
                    Usuarios
                </a>
            </li>
            <li>
                <a onclick="closeSession()">
                    Cerrar Sesion
                </a>
            </li>
        </ul>
    </div>
    <!-- /#sidebar-wrapper -->

    <!-- Page Content -->
    <div id="page-content-wrapper">
        <div class="container-fluid">
            <div class="row">
                <i class="fa fa-bars" aria-hidden="true" id="menu-toggle" href="#menu-toggle"></i>
            </div>
            <div class="container">
                <div class="col-md-12">
                    <div class="col-md-4">
                        <div class="input-field">
                            <input name="id" id="id" type="number" class="black-text">
                            <label for="id">Codigo</label>
                        </div>
                    </div>
                    <div class="col-md-8">
                        <div class="input-field">
                            <input name="name" id="name" type="text" class="black-text">
                            <label for="name">Nombre del producto</label>
                        </div>
                    </div>
                </div>
                
                <div class="col-md-12">
                    <div class="col-md-6">
                        <div class="input-field">
                            <input name="minimum" id="minimum" type="number" class="black-text" value="0">
                            <label for="minimum">Minimo de unidades</label>
                        </div>
                    </div>
                    <div class="col-md-6">
                        <div class="input-field">
                            <input name="iva" id="iva" type="number" class="black-text" value="19">
                            <label for="iva">IVA</label>
                        </div>
                    </div>
                </div>

                <div class="col-md-12">
                    <div class="col-md-4">
                        <h6>Marca: </h6>
                    </div>
                    <div class="col-md-8">
                        <select id="brands" class="input-large form-control">
                        </select>
                    </div>
                </div>

                <div class="col-md-12" style="margin-top: 4%">
                    <div class="col-md-4">
                        <h6>Categoria: </h6>
                    </div>
                    <div class="col-md-8">
                        <select id="categories" class="input-large form-control">
                        </select>
                    </div>
                </div>

                <br>
                <br>

                <div class="col-md-12 text-center">
                    <h5>Precios (CON IVA)</h5>
                </div>

                <div class="col-md-12">
                    <div class="col-md-6 col-sm-6">
                        <div class="input-field">
                            <input name="dis-1" id="dis-1" type="number" value="0">
                            <label for="dis-1">Centro</label>
                        </div>
                    </div>
                    <div class="col-md-6 col-sm-6">
                        <div class="input-field">
                            <input name="dis-2" id="dis-2" type="number" value="0">
                            <label for="dis-2">Nacional</label>
                        </div>
                    </div>
                    <div class="col-md-6 col-sm-6">
                        <div class="input-field">
                            <input name="dis-3" id="dis-3" type="number" value="0">
                            <label for="dis-3">Periferia</label>
                        </div>
                    </div>
                    <div class="col-md-6 col-sm-6">
                        <div class="input-field">
                            <input name="dis-4" id="dis-4" type="number" value="0">
                            <label for="dis-4">Peluquerias</label>
                        </div>
                    </div>
                    <div class="col-md-12 col-sm-12">
                        <div class="input-field">
                            <input name="dis-5" id="dis-5" type="number" value="0">
                            <label for="dis-5">Cliente final</label>
                        </div>
                    </div>
                </div>

                <div class="col-md-12 text-center">
                    <h5>Descuentos credito pronto pago</h5>
                </div>

                <div class="col-md-12">
                    <div class="col-md-6 col-sm-6">
                        <div class="input-field">
                            <input name="pay-1" id="pay-1" type="number" value="0">
                            <label for="pay-1">Pronto pago 0 dias</label>
                        </div>
                    </div>
                    <div class="col-md-6 col-sm-6">
                        <div class="input-field">
                            <input name="pay-2" id="pay-2" type="number" value="0">
                            <label for="pay-2">Pronto pago 8 dias</label>
                        </div>
                    </div>
                    <div class="col-md-6 col-sm-6">
                        <div class="input-field">
                            <input name="pay-3" id="pay-3" type="number" value="0">
                            <label for="pay-3">Pronto pago 15 dias</label>
                        </div>
                    </div>
                    <div class="col-md-6 col-sm-6">
                        <div class="input-field">
                            <input name="pay-4" id="pay-4" type="number" value="0" >
                            <label for="pay-4">Pronto pago 30 dias</label>
                        </div>
                    </div>
                </div>

                <div class="row text-center">
                    <a class="waves-effect waves-light btn white-text" name="btn-login" type="submit" id="btn-login"
                       onClick="sendData()">CREAR PRODUCTO</a>
                </div>
            </div>
        </div>
    </div>
    <!-- /#page-content-wrapper -->

</div>
<!-- /#wrapper -->

<script type="text/javascript" src="<?php echo base_url('assets/js/general.js'); ?>"></script>
<script type="text/javascript" src="<?php echo base_url('assets/js/constants.js'); ?>"></script>
<script src="<?php echo base_url('assets/js/jquery.js'); ?>"></script>
<script src="<?php echo base_url('assets/js/item_create.js'); ?>"></script>
<script src="<?php echo base_url('assets/js/bootstrap.min.js'); ?>"></script>
<script type="text/javascript" src="<?php echo base_url('assets/js/jquery.loading.min.js'); ?>"></script>
<script src="<?php echo base_url('assets/js/materialize.min.js'); ?>"></script>
<link rel="stylesheet" href="//cdnjs.cloudflare.com/ajax/libs/bootstrap-table/1.11.0/bootstrap-table.min.css">
<link href="//cdnjs.cloudflare.com/ajax/libs/x-editable/1.5.0/bootstrap3-editable/css/bootstrap-editable.css"
      rel="stylesheet"/>
<script
    src="//cdnjs.cloudflare.com/ajax/libs/x-editable/1.5.0/bootstrap3-editable/js/bootstrap-editable.min.js"></script>
<!-- Latest compiled and minified JavaScript -->
<script src="//cdnjs.cloudflare.com/ajax/libs/bootstrap-table/1.11.0/bootstrap-table.min.js"></script>

<!-- Latest compiled and minified Locales -->
<script src="//cdnjs.cloudflare.com/ajax/libs/bootstrap-table/1.11.0/locale/bootstrap-table-es-ES.min.js"></script>

<script>
    $("#menu-toggle").click(function (e) {
        e.preventDefault();
        $("#wrapper").toggleClass("toggled");
    });
</script>

</body>

</html>