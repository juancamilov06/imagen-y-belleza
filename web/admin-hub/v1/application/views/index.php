<html>
	<head>
		<link href="<?php echo base_url('assets/css/bootstrap.min.css');?>" rel="stylesheet">
		<script type="text/javascript" src="<?php echo base_url('assets/js/constants.js');?>"></script>
		<script src="<?php echo base_url('assets/js/jquery.js');?>"></script>
    	<link rel="stylesheet" type="text/css" href="<?php echo base_url('assets/css/loading.min.css');?>">
		<link rel="stylesheet" type="text/css" href="<?php echo base_url('assets/css/materialize.min.css');?>">
		<script type="text/javascript" src="<?php echo base_url('assets/js/materialize.min.js');?>"></script>
       	<script src="<?php echo base_url('assets/js/bootstrap.min.js');?>"></script>
       	<script type="text/javascript" src="<?php echo base_url('assets/js/login.js');?>"></script>
       	<link href="<?php echo base_url('assets/css/index.css');?>" rel="stylesheet">
		<script type="text/javascript" src="<?php echo base_url('assets/js/jquery.loading.min.js');?>"></script>
	</head>
	<body>
		<div class="container">
			<div class="row">
				<img src="<?php echo base_url('assets/res/app_logo.png');?>" class="center-block resize">
			</div>
			<div class="row">
				<div class="col-md-6 col-sm-6 col-xs-8 center-block" id="form-container">
					<form id="login">
						<div class="input-field">
				          <input name="username" id="username" type="text" class="white-text">
				          <label for="username">Nombre de usuario</label>
				        </div>
				        <div class="input-field">
				          <input name="password" id="password" type="password" class="white-text">
				          <label for="password">Contraseña</label>
				        </div>
				        <div class="row text-center">
				        	<a class="waves-effect waves-light btn white-text" name="btn-login" type="submit" id="btn-login" onClick="sendData($('#username').val(), $('#password').val())">Ingresar</a>
				        </div>
					</form>						
				</div>
			</div>
			<div class="row text-center">
				<div class="err row" id="add_err"></div>
			</div>
		</div>
	</body>
</html>