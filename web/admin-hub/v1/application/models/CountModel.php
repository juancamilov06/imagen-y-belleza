<?php


if ( ! defined('BASEPATH')) exit('No direct script access allowed');

class CountModel extends CI_Model{

    function __construct(){
        parent::__construct();
    }

    public function getAll(){

        $countArray = array();

        $userQuery = "SELECT count(*) as conteo FROM user";
        $results = $this->db->query($userQuery);

        $count = new stdClass();
        $count->itemCount = $results->row(0)->conteo;
        $count->table = "user";

        array_push($countArray, $count);

        $itemQuery = "SELECT count(*) as conteo FROM item";
        $results = $this->db->query($itemQuery);

        $count = new stdClass();
        $count->itemCount = $results->row(0)->conteo;
        $count->table = "item";

        array_push($countArray, $count);

        $orderQuery = "SELECT count(*) as conteo FROM order_";
        $results = $this->db->query($orderQuery);

        $count = new stdClass();
        $count->itemCount = $results->row(0)->conteo;
        $count->table = "order_";

        array_push($countArray, $count);

        $orderItemsQuery = "SELECT count(*) as conteo FROM order_items";
        $results = $this->db->query($orderItemsQuery);

        $count = new stdClass();
        $count->itemCount = $results->row(0)->conteo;
        $count->table = "order_items";

        array_push($countArray, $count);

        $clientQuery = "SELECT count(*) as conteo FROM client";
        $results = $this->db->query($clientQuery);

        $count = new stdClass();
        $count->itemCount = $results->row(0)->conteo;
        $count->table = "client";

        array_push($countArray, $count);

        $brandQuery = "SELECT count(*) as conteo FROM brand";
        $results = $this->db->query($brandQuery);

        $count = new stdClass();
        $count->itemCount = $results->row(0)->conteo;
        $count->table = "brand";

        array_push($countArray, $count);

        $results = new stdClass();
        $results->results = $countArray;

        return $results;
    }

}