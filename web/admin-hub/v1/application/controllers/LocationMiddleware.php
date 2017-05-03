<?php

if ( ! defined('BASEPATH')) exit('No direct script access allowed');

require APPPATH . "/libraries/REST_Controller.php";
use Restserver\Libraries\REST_Controller;

class LocationMiddleware extends REST_Controller
{

    public function __construct()
    {
        parent::__construct();
        $this->load->model('LocationModel');
    }

    public function all_get(){

        $locations = $this->LocationModel->getAll();
        echo json_encode($locations, JSON_UNESCAPED_UNICODE);

    }

    public function create_post(){

        $locations = json_decode($this->post('locations'));
        $response = $this->LocationModel->insertAll($locations);

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