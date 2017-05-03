<?php

require APPPATH . "/libraries/REST_Controller.php";
use Restserver\Libraries\REST_Controller;

class CategoriesMiddleware extends REST_Controller
{

    public function __construct()
    {
        parent::__construct();
        $this->load->model('CategoriesModel');
    }

    public function index_get(){

        $this->load->view('categories');

    }

    public function all_get(){

        $categories = $this->CategoriesModel->getAll();
        echo json_encode($categories, JSON_UNESCAPED_UNICODE);

    }

    public function update_post(){

        $id = $this->post('id');
        $name = $this->post('name');

        if($this->CategoriesModel->updateCategory($id, $name)){
            print 'true';
        } else {
            print 'false';
        }

    }

}