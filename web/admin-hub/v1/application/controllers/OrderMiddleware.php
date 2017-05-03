<?php

require APPPATH . "/libraries/REST_Controller.php";
use Restserver\Libraries\REST_Controller;

class OrderMiddleware extends REST_Controller {

    public function __construct(){
        parent::__construct();
        $this->load->model('OrderModel');
        $this->load->model('OrderItemModel');
    }

    public function detailindex_get(){

        $this->load->view('order_detail');

    }

    public function detail_get($orderId){

        $results = $this->OrderModel->getOrderDetail($orderId);
        echo json_encode($results, JSON_UNESCAPED_UNICODE);
    }

    public function hubupdate_post(){

        $orderId = $this->post('order_id');
        $response = $this->OrderModel->updateOrderHub($orderId);
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

    public function huball_get(){

        $results = $this->OrderModel->getAllHub();
        echo json_encode($results, JSON_UNESCAPED_UNICODE);

    }

    public function all_get(){

        $orders = $this->OrderModel->getOrders();
        echo json_encode($orders, JSON_UNESCAPED_UNICODE);

    }

    public function storageall_get(){

        $data = $this->OrderModel->getStorageAll();
        echo json_encode($data, JSON_UNESCAPED_UNICODE);

    }
    
    public function create_post(){

        $values = json_decode($this->post('orders'));
        $response = $this->OrderModel->createAll($values);

        if ($response) {

            $items = json_decode($this->post('items'));
            $itemsResponse = $this->OrderItemModel->create($items);

            if ($itemsResponse) {
                print json_encode(array(
                    'mensaje'=>'Creacion exitosa'
                ));
            } else {
                print json_encode(array(
                    'mensaje'=>'Creacion fallida'
                ));
            }

        } else {
            print json_encode(array(
                'mensaje'=>'Creacion fallida'
            ));
        }
        
    }
}