<?php

defined('BASEPATH') OR exit('No direct script access allowed');

class LocationModel extends CI_Model {

    function __construct(){
        parent::__construct();
    }

    public function getAll(){

        $date = date('Y-m-d', time());

        $locationQuery = "SELECT * FROM location WHERE DATEDIFF(NOW(),created) < 30";
        $query = $this->db->query($locationQuery);
        $results = $query->result_array();

        $object = new stdClass();
        $object->locations = $results;

        return $results;

    }

    public function insertAll($locations){

        $error = false;
        $insertQuery = 'INSERT INTO location(latitude, longitude, created, seller_id) VALUES (?,?,?,?)';
        foreach($locations as $json) {
            if($this->db->query($insertQuery, array(
                $json->latitude,
                $json->longitude,
                $json->created,
                $json->seller_id
            ))){
                $error = true;
            } else {
                $error = false;
            }
        }

        return $error;

    }



}