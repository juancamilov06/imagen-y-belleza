$(document).ready(function(){
	getSession();
	$.showLoading({
        name: 'square-flip'
    });
	$('#table').bootstrapTable({
		url: 'http://' + ip + '/admin-hub/v1.1/orders/hub_all',
		search: true,
		pagination: true,
		columns: [{
			field: 'id',
			title: 'Codigo'
		}, {
			field: 'deliver',
			sortable: true,
			title: 'Fecha de entrega'
		}, {
			field: 'modified',
			sortable: true,
			title: 'Modificado'
		},{
			field: 'client_id',
			sortable: true,
			title: 'Cliente'
		},{
			field: 'seller_id',
			sortable: true,
			title: 'Vendedor'
		},{
			field: 'payment_id',
			sortable: true,
			title: 'Pago'
		},{
			field: 'order_state_id',
			sortable: true,
			title: 'Estado'
		},{
			field: 'biller_id',
			sortable: true,
			title: 'Facturador'
		}
		],
		onClickRow: function (row, $element) {
			window.location.replace("orders/detail?order_id=" + row.id);
		},
		onLoadSuccess: function (data) {
			$.hideLoading(); 
			toggle();
		}, 
		onLoadError: function (error){
			$.hideLoading(); 
		}
	});
});

function getSession(){
	if (localStorage.getItem("role") == null) {
		window.location.replace("/admin-hub/v1.1/");
	}
}