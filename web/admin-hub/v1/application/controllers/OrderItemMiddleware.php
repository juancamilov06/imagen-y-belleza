<?php

if ( ! defined('BASEPATH')) exit('No direct script access allowed');

require APPPATH . "/libraries/REST_Controller.php";
use Restserver\Libraries\REST_Controller;

class OrderItemMiddleware extends REST_Controller{

    public function __construct(){
        parent::__construct();
        $this->load->model('OrderItemModel');
    }
    
    public function find_get($orderId){

        $results = $this->OrderItemModel->getOrderItems($orderId);
        echo json_encode($results, JSON_UNESCAPED_UNICODE);

    }

    public function create_post(){

        $orderItems = json_decode($this->post('order_items'));
        $orders = json_decode($this->post('orders'));

        $response = $this->OrderItemModel->updateItems($orderItems);

        if (isset($orders->orders)){
            if ($response) {
                print json_encode(array(
                    'mensaje'=>'Creacion exitosa'
                ));
            } else {
                print json_encode(array(
                    'mensaje'=>'Creacion fallida'
                ));
            }
        } else {
            $orderResponse = $this->OrderItemModel->updateOrders($orders);

            if ($response && $orderResponse) {
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

}