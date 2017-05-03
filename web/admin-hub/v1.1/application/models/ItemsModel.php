<?php

defined('BASEPATH') OR exit('No direct script access allowed');

class ItemsModel extends CI_Model {

    function __construct(){
        parent::__construct();
    }

    public function createItem($item){

        $versionQuery = "UPDATE version SET date = NOW() WHERE id = 1";
        $this->db->query($versionQuery);

        $insertQuery = "INSERT INTO `item`(`id`, `brand_id`, `category_id`, `subitem_id`, `name`, `is_active`
                        , `is_new`, `iva`, `available_units`, `minimum_available`, `price_one`, `price_two`, `price_three`, `price_four`, `price_five`
                        , `payment_one`, `payment_two`, `payment_three`, `payment_four`, `created`, `modified`) VALUES (?,?,?,0,?,?,?,?,0,?,?,?,?,?,?,?,?,?,?,NOW(),NOW())";
        
        return $this->db->query($insertQuery, array($item->id, $item->brand_id, $item->category_id, $item->name, $item->is_active, $item->is_new
            , $item->iva, $item->minimum_units, $item->price_one, $item->price_two, $item->price_three,$item->price_four
            , $item->price_five, $item->payment_one, $item->payment_two, $item->payment_three, $item->payment_four));

    }

    public function getAll(){

        $statement = "SELECT * FROM item";
        $query = $this->db->query($statement);
        return $query->result_array();

    }

    public function updateItem($item){

        $versionQuery = "UPDATE version SET date = NOW() WHERE id = 1";
        $this->db->query($versionQuery);

        $updateQuery = "UPDATE item SET name = ?, iva = ?, is_active = ?, payment_one = ?, payment_two = ?, payment_three = ?, payment_four = ?, modified = NOW() WHERE id = ?";
        return $this->db->query($updateQuery, array($item->name, $item->iva
                                , $item->is_active, $item->payment_one, $item->payment_two
                                , $item->payment_three, $item->payment_four, $item->id));

    }

    public function get($id){
        $query = $this->db->select('*')->from('item')->where('id', $id)->get();
        return $query->row_array();
    }

}