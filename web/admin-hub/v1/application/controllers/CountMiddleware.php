<?php

require APPPATH . "/libraries/REST_Controller.php";
use Restserver\Libraries\REST_Controller;

class CountMiddleware extends REST_Controller {

    public function __construct(){
        parent::__construct();
        $this->load->model('CountModel');
    }

    public function count_get(){

        $count = $this->CountModel->getAll();
        echo json_encode($count, JSON_UNESCAPED_UNICODE);

    }
}