<?php

require APPPATH . "/libraries/REST_Controller.php";
use Restserver\Libraries\REST_Controller;

class EnteringMiddleware extends REST_Controller
{

    public function __construct()
    {
        parent::__construct();
        $this->load->model('EnteringModel');
    }

    public function index_get(){

        $this->load->view('item_entering');

    }

    public function create_post(){

        $entering = json_decode($this->post('entering'));
        $response = $this->EnteringModel->create($entering);

        if ($response) {
            print 'Creado';
        } else {
            print 'Error';
        }

    }

}