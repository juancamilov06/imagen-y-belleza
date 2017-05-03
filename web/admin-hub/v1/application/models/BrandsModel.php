<?php

if ( ! defined('BASEPATH')) exit('No direct script access allowed');

class BrandsModel extends CI_Model
{

    function __construct()
    {
        parent::__construct();
    }

    public function getAll(){

        $statement = "SELECT * FROM brand";
        $query = $this->db->query($statement);
        return $query->result_array();
    }

    public function find($id){
        $statement = "SELECT * FROM brand WHERE id = " . $id;
        $query = $this->db->query($statement);
        return $query->row_array();
    }

    public function updateBrand($id, $name){

        $versionQuery = "UPDATE version SET date = NOW() WHERE id = 1";
        $this->db->query($versionQuery);

        $updateQuery = "UPDATE brand SET name = ?, modified = NOW() WHERE id = ?";
        return $this->db->query($updateQuery, array($name, $id));

    }

}