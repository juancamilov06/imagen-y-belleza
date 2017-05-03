<?php

require APPPATH . "/libraries/REST_Controller.php";
use Restserver\Libraries\REST_Controller;

class DisposalMiddleware extends REST_Controller
{

    public function __construct()
    {
        parent::__construct();
        $this->load->model('DisposalModel');
    }

    public function index_get(){

        $this->load->view('item_disposal');

    }

    public function create_post(){

        $disposal = json_decode($this->post('disposal'));
        $response = $this->DisposalModel->create($disposal);

        if ($response) {
            print 'Creado';
        } else {
            print 'Error';
        }
        
    }

}