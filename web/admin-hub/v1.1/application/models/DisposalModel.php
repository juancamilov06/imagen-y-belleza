<?php

if ( ! defined('BASEPATH')) exit('No direct script access allowed');

class DisposalModel extends CI_Model
{

    function __construct()
    {
        parent::__construct();
    }

    public function create($disposal){

        $itemQuery = "SELECT * FROM item WHERE id = " . $disposal->item_id;
        $query = $this->db->query($itemQuery);
        $results = (object) $query->row_array();

        $unitPrice = $results->price;

        $insertQuery = "INSERT INTO item_disposal(item_id, units, total, disposal_date, concept) VALUES (?,?,?,NOW(),?)";
        $error = $this->db->query($insertQuery, array($disposal->item_id,
            $disposal->units,
            $unitPrice * $disposal->units,
            $disposal->concept
        ));

        $updateQuery = "UPDATE item SET available_units = available_units - ?, modified = NOW() WHERE id = ?";
        $error = $this->db->query($updateQuery, array($disposal->units, $disposal->item_id));

        return $error;

    }

}