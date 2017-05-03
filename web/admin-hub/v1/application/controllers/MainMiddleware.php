<?php

if ( ! defined('BASEPATH')) exit('No direct script access allowed');

require APPPATH . "/libraries/REST_Controller.php";
use Restserver\Libraries\REST_Controller;

class MainMiddleware extends REST_Controller
{
    public function __construct()
    {
        parent::__construct();
    }
    
    public function index_get(){
        $this->load->view('main.php');
    }
    
}