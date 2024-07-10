<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use App\Models\School;

class SchoolController extends Controller
{
    public function create()
    {
        return view('school.create');
    }

    public function store(Request $request)
    {
        $validatedData = $request->validate([
            'schoolRegistrationNumber' => 'required|string|max:20',
            'schoolName' => 'required|string|max:25',
            'schoolRepresentativeName' => 'required|string|max:20',
            'schoolRepresentativeEmail' => 'required|string|max:25',
            'district' => 'required|string|max:25'
        ]);

        // Create a new school record
        School::create($validatedData);

        return redirect()->route('school.index')->with('success', 'School registered successfully!');
    }
}

