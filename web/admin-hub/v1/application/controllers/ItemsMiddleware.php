<?php

if ( ! defined('BASEPATH')) exit('No direct script access allowed');

require APPPATH . "/libraries/REST_Controller.php";
use Restserver\Libraries\REST_Controller;

class ItemsMiddleware extends REST_Controller{

    public function __construct()
    {
        parent::__construct();
        $this->load->model('ItemsModel');
    }

    public function index_get(){

        $this->load->view('item');

    }

    public function indexcreate_get(){

        $this->load->view('item_create');

    }
    
    public function all_get(){

        $items = $this->ItemsModel->getAll();
        if (!is_null($items)){
            echo json_encode($items, JSON_UNESCAPED_UNICODE);
        } else {
            $this->response(null, 400);
        }

    }

    public function create_post(){

        $item = $this->post('item');
        $decoded = json_decode($item);
        $response = $this->ItemsModel->createItem($decoded);
        if ($response){
            echo 'Creado';
        } else {
            echo 'Error';
        }

    }

	public function find_get($id){

        if (!$id){
            $this->response(null, 400);
        } else {
            $item = $this->ItemsModel->get($id);
            if (!is_null($item)){
                echo json_encode($item, JSON_UNESCAPED_UNICODE);
            } else {
                $this->response(array('error' => 'No hay productos con el id asociado'), 404);
            }
        }

	}

    public function update_post(){

        $it = $this->post('item');
        $encoded = json_decode($it);
        if ($this->ItemsModel->updateItem($encoded)){
            echo 'true';
        } else {
            echo 'false';
        }

    }
}  