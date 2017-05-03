<?php

if ( ! defined('BASEPATH')) exit('No direct script access allowed');

class CategoriesModel extends CI_Model
{

    function __construct()
    {
        parent::__construct();
    }

    public function getAll(){

        $statement = "SELECT * FROM category";
        $query = $this->db->query($statement);
        return $query->result_array();
    }

    public function updateCategory($id, $name){

        $versionQuery = "UPDATE version SET date = NOW() WHERE id = 1";
        $this->db->query($versionQuery);

        $updateQuery = "UPDATE category SET name = ?, modified = NOW() WHERE id = ?";
        return $this->db->query($updateQuery, array($name, $id));

    }

}