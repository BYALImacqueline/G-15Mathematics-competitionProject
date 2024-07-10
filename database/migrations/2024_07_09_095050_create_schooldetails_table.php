<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        Schema::create('schooldetails', function (Blueprint $table) {
            $table->string('schoolRegistrationNumber');
            $table->string('schoolName');
            $table->string('schoolRepresentativeName');
            $table->string('schoolRepresentativeEmail');
            $table->string('district');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('schooldetails');
    }
};
