<?php

namespace App\Http\Controllers;

use Illuminate\Foundation\Auth\AuthenticatesUsers;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;

class LoginController extends Controller
{
    use AuthenticatesUsers;

    /**
     * Get the post login redirect path.
     *
     * @var string
     */
    protected function redirectTo()
    {
        if (Auth::check() && Auth::user()->usertype == 'admin') {
            return 'dashboard'; // Redirect to admin dashboard route
        } else {
            return 'home'; // Redirect to regular user dashboard route
        }
    }
}
