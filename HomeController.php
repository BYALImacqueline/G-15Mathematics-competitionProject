<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;

class HomeController extends Controller
{
    public function index()
    {
        // Logic for your home page
        return view('welcome'); // Assuming 'welcome.blade.php' exists in resources/views
    }
}
