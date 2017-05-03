<!DOCTYPE html>
<html lang="en">

<head>

    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, shrink-to-fit=no, initial-scale=1">
    <meta name="description" content="">
    <meta name="author" content="">

    <title>Administración</title>

    <link href="<?php echo base_url('assets/css/bootstrap.min.css');?>" rel="stylesheet">
    <link rel="stylesheet" type="text/css" href="<?php echo base_url('assets/css/font-awesome.min.css');?>">
    <link rel="stylesheet" href="<?php echo base_url('assets/remodal/dist/remodal.css');?>">
    <link rel="stylesheet" href="<?php echo base_url('assets/remodal/dist/remodal-default-theme.css');?>">
    <link rel="stylesheet" type="text/css" href="<?php echo base_url('assets/css/materialize.min.css');?>">
    <link href="<?php echo base_url('assets/css/simple-sidebar.css');?>" rel="stylesheet">
    <link rel="stylesheet" type="text/css" href="<?php echo base_url('assets/css/main.css');?>">
    <link rel="stylesheet" type="text/css" href="<?php echo base_url('assets/css/loading.min.css');?>">

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
                    <a href="main">
                        Pedidos
                    </a>
                </li>
                <li>
                    <a href="clients">
                        Clientes
                    </a>
                </li>
                <li>
                    <a href="brands">
                        Marcas
                    </a>
                </li>
                <li>
                    <a href="categories">
                        Categorias
                    </a>
                </li>
                <li>
                    <a href="inventory">
                        Inventario
                    </a>
                </li>
                <li>
                    <a onclick="toggle()">
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
                <div class="container table-container">
                    <div class="row">
                        <div class="col-md-12 col-lg-12 col-sm-12 col-xs-12">
                            <table id="table"></table>
                        </div>
                    </div>
                </div>             
            </div>
        </div>
        <!-- /#page-content-wrapper -->

        <div class="remodal" data-remodal-id="modal">
            <h1>Editar usuario</h1>

            <div class="col-md-12 col-sm-6">
                <div class="col-md-6">
                    <div class="input-field">
                        <input name="id" id="id" type="text" disabled>
                        <label for="id">Identificador interno</label>
                    </div>
                </div>
                <div class="col-md-6">
                    <div class="input-field">
                        <input name="username" id="username" type="text" >
                        <label for="username">Nombre de usuario</label>
                    </div>
                </div>
            </div>

            <div class="col-md-12 col-sm-6">
                <div class="col-md-12">
                    <div class="input-field">
                        <input name="contact" id="contact" type="text" >
                        <label for="contact">Empleado</label>
                    </div>
                </div>
            </div>


            <div class="col-md-12">
                <div class="col-md-6 col-sm-6">
                    <select id="roles" class="input-large form-control">
                        <option value="1">Admin</option>
                        <option value="2">Facturacion</option>
                        <option value="3">Vendedor</option>
                    </select>
                </div>
                <div class="col-md-6 col-sm-6">
                    <select id="active" class="input-large form-control">
                        <option value="1">Activo</option>
                        <option value="2">Inactivo</option>
                    </select>
                </div>
            </div>

            <br>
            <button data-remodal-action="cancel" class="remodal-cancel" style="margin-top: 5%">Cancelar</button>
            <button data-remodal-action="confirm" class="remodal-confirm" style="margin-top: 5%">Actualizar</button>
        </div>

    </div>
    <!-- /#wrapper -->
    <script type="text/javascript" src="<?php echo base_url('assets/js/general.js');?>"></script>
    <script type="text/javascript" src="<?php echo base_url('assets/js/constants.js');?>"></script>
    <script src="<?php echo base_url('assets/js/jquery.js');?>"></script>
    <script src="<?php echo base_url('assets/js/bootstrap.min.js');?>"></script>
    <script type="text/javascript" src="<?php echo base_url('assets/js/jquery.loading.min.js');?>"></script>
    <script src="<?php echo base_url('assets/remodal/dist/remodal.min.js');?>"></script>
    <script type="text/javascript" src="<?php echo base_url('assets/js/materialize.min.js');?>"></script>
    <link rel="stylesheet" href="//cdnjs.cloudflare.com/ajax/libs/bootstrap-table/1.11.0/bootstrap-table.min.css">
    <link href="//cdnjs.cloudflare.com/ajax/libs/x-editable/1.5.0/bootstrap3-editable/css/bootstrap-editable.css" rel="stylesheet"/>
    <script src="//cdnjs.cloudflare.com/ajax/libs/x-editable/1.5.0/bootstrap3-editable/js/bootstrap-editable.min.js"></script>
    <!-- Latest compiled and minified JavaScript -->
    <script src="//cdnjs.cloudflare.com/ajax/libs/bootstrap-table/1.11.0/bootstrap-table.min.js"></script>

    <!-- Latest compiled and minified Locales -->
    <script src="//cdnjs.cloudflare.com/ajax/libs/bootstrap-table/1.11.0/locale/bootstrap-table-es-ES.min.js"></script>
    <script src="<?php echo base_url('assets/js/bootstrap-table-editable.js');?>"></script>
    <script src="<?php echo base_url('assets/js/user.js');?>"></script>

    <script>
    $("#menu-toggle").click(function(e) {
        e.preventDefault();
        $("#wrapper").toggleClass("toggled");
    });
    </script>

</body>

</html>