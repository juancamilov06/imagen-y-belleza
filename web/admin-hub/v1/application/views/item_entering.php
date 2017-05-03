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
                    <a href="users">
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
                    <h6>Selecciona el proveedor</h6>
                    <select id="providers" class="input-large form-control">
                    </select>
                    <h6>Selecciona el producto</h6>
                    <select id="items" class="input-large form-control">
                    </select>
                    </br>
                    </br>
                    <div class="col-md-12">
                        <div class="col-md-6">
                            <div class="input-field">
                              <input name="units" id="units" type="number" value="0" class="black-text">
                              <label for="units">Unidades</label>
                          </div>
                      </div>
                      <div class="col-md-6">
                       <div class="input-field">
                          <input name="price" id="price" type="number" value="0" class="black-text">
                          <label for="price">Precio unitario</label>
                      </div>
                  </div>
              </div>
              <div class="col-md-12">
                <div class="col-md-6">
                    <div class="input-field">
                      <input name="discount" id="discount" value="0" type="number" class="black-text">
                      <label for="discount">Descuento</label>
                  </div>
              </div>
              <div class="col-md-6">
               <div class="input-field">
                  <input name="iva" id="iva" type="number" value="19" class="black-text">
                  <label for="iva">IVA</label>
              </div>
          </div>
      </div>
      <div class="col-md-12">
           <div class="input-field">
                      <input name="notes" id="notes" placeholder="Notas de la entrada" type="text" class="black-text">
                      <label for="notes">Notas</label>
                  </div>
      </div>
      
      <div class="row text-center">
        <a class="waves-effect waves-light btn white-text" name="btn-login" type="submit" id="btn-login" onClick="sendData()">CREAR INGRESO</a>
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
    <script src="<?php echo base_url('assets/js/item_entering.js'); ?>"></script>
    <script src="<?php echo base_url('assets/js/bootstrap.min.js'); ?>"></script>
    <script type="text/javascript" src="<?php echo base_url('assets/js/jquery.loading.min.js'); ?>"></script>
    <script src="<?php echo base_url('assets/js/materialize.min.js'); ?>"></script>
<link rel="stylesheet" href="//cdnjs.cloudflare.com/ajax/libs/bootstrap-table/1.11.0/bootstrap-table.min.css">
<link href="//cdnjs.cloudflare.com/ajax/libs/x-editable/1.5.0/bootstrap3-editable/css/bootstrap-editable.css" rel="stylesheet"/>
<script src="//cdnjs.cloudflare.com/ajax/libs/x-editable/1.5.0/bootstrap3-editable/js/bootstrap-editable.min.js"></script>
<!-- Latest compiled and minified JavaScript -->
<script src="//cdnjs.cloudflare.com/ajax/libs/bootstrap-table/1.11.0/bootstrap-table.min.js"></script>

<!-- Latest compiled and minified Locales -->
<script src="//cdnjs.cloudflare.com/ajax/libs/bootstrap-table/1.11.0/locale/bootstrap-table-es-ES.min.js"></script>

<script>
$("#menu-toggle").click(function(e) {
    e.preventDefault();
    $("#wrapper").toggleClass("toggled");
});
</script>

</body>

</html>