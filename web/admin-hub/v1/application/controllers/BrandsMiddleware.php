<?php

require APPPATH . "/libraries/REST_Controller.php";
use Restserver\Libraries\REST_Controller;

class BrandsMiddleware extends REST_Controller
{

    public function __construct()
    {
        parent::__construct();
        $this->load->model('BrandsModel');
    }

    public function index_get(){
        $this->load->view('brands');
    }
    
    public function find_get($id){
        $brand = $this->BrandsModel->find($id);
        echo json_encode($brand, JSON_UNESCAPED_UNICODE);
    }

    public function all_get(){
        
        $brands = $this->BrandsModel->getAll();
        echo json_encode($brands, JSON_UNESCAPED_UNICODE);
        
    }

    public function update_post(){

        $id = $this->post('id');
        $name = $this->post('name');

        if($this->BrandsModel->updateBrand($id, $name)){
            print 'true';
        } else {
            print 'false';
        }

    }

}