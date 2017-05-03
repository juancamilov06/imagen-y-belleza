<?php

require APPPATH . "/libraries/REST_Controller.php";
use Restserver\Libraries\REST_Controller;

class BillMiddleware extends REST_Controller {

    public function __construct(){
        parent::__construct();
        $this->load->model('BillModel');
    }
    
    public function unbilled_get(){

        $unbilledOrders = $this->BillModel->getUnbilledOrders();
        echo json_encode($unbilledOrders, JSON_UNESCAPED_UNICODE);
        
    }

    public function updateall_post(){
        
        $orders =  json_decode($this->post('orders'));
        $response = $this->BillModel->updateOrders($orders);

        if ($response) {
            print json_encode(array(
                'mensaje'=>'Creacion exitosa'
            ));
        } else {
            print json_encode(array(
                'mensaje'=>'Creacion fallida'
            ));
        }

    }

    public function finish_post(){

        $id = $this->post('id');
        $billerId = $this->post('biller_id');
        $stateId = $this->post('state_id');
        $orderItems = json_decode($this->post('order_items'));

        $response = $this->BillModel->finishOrder($id, $stateId, $billerId, $orderItems);

        if ($response) {
            print json_encode(array(
                'mensaje'=>'Creacion exitosa'
            ));
        } else {
            print json_encode(array(
                'mensaje'=>'Creacion fallida'
            ));
        }

    }

    public function update_post(){
        $id =  $this->post('id');
        $stateId = $this->post('state_id');
        $billerId = $this->post('biller_id');

        $response = $this->BillModel->updateOrder($id, $stateId, $billerId);

        if ($response) {
            print json_encode(array(
                'mensaje'=>'Creacion exitosa'
            ));
        } else {
            print json_encode(array(
                'mensaje'=>'Creacion fallida'
            ));
        }

    }

}