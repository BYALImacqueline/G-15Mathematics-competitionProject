<x-layout bodyClass="">

    <div>
        <div class="container position-sticky z-index-sticky top-0">
            <div class="row">
                <div class="col-12">
                    <!-- Navbar -->
                    <x-navbars.navs.guest signin='login' signup='register'></x-navbars.navs.guest>
                    <!-- End Navbar -->
                </div>
            </div>
        </div>
        <main class="main-content  mt-0">
            <section>
                <div class="page-header min-vh-100">
                    <div class="container">
                        <div class="row">
                            <div
                                class="col-6 d-lg-flex d-none h-100 my-auto pe-0 position-absolute top-0 start-0 text-center justify-content-center flex-column">
                                <div class="position-relative bg-gradient-primary h-100 m-3 px-7 border-radius-lg d-flex flex-column justify-content-center"
                                    style="background-image: url('../assets/img/illustrations/illustration-signup.jpg'); background-size: cover;">
                                </div>
                            </div>
                            <div
                                class="col-xl-4 col-lg-5 col-md-7 d-flex flex-column ms-auto me-auto ms-lg-auto me-lg-5">
                                <div class="card card-plain">
                                    <div class="card-header">
                                        <h4 class="font-weight-bolder">REGISTER</h4>
                                        <p class="mb-0"><b>Enter the school details</b> </p>
                                    </div>
                                    <div class="card-body">
                                    <form method="POST" action="{{ route('school.store') }}">
                                       @csrf


                                    <div class="input-group input-group-outline mt-3">
                                                <label class="form-label">School Registration Number</label>
                                                <input type="text" class="form-control" name="registrationnumber"
                                                    value="{{ old('registrationnumber') }}">
                                            </div>
                                            @error('registrationnumber')
                                            <p class='text-danger inputerror'>{{ $message }} </p>
                                            @enderror 


                                        <div class="input-group input-group-outline mt-3">
                                                <label class="form-label">School Name</label>
                                                <input type="text" class="form-control" name="schoolname"
                                                    value="{{ old('schoolname') }}">
                                            </div>
                                            @error('schoolname')
                                            <p class='text-danger inputerror'>{{ $message }} </p>
                                            @enderror


                                            <div class="input-group input-group-outline mt-3">
                                                <label class="form-label">School Representative Name</label>
                                                <input type="text" class="form-control" name="representativename"
                                                    value="{{ old('representativename') }}">
                                            </div>
                                            @error('representativename')
                                            <p class='text-danger inputerror'>{{ $message }} </p>
                                            @enderror

                                            <div class="input-group input-group-outline mt-3">
                                                <label class="form-label">School Representative Email</label>
                                                <input type="email" class="form-control" name="email"
                                                    value="{{ old('email') }}">
                                            </div>
                                            @error('email')
                                            <p class='text-danger inputerror'>{{ $message }} </p>
                                            @enderror

                                            <div class="input-group input-group-outline mt-3">
                                                <label class="form-label">District</label>
                                                <input type="district" class="form-control" name="district"
                                                    value="{{ old('district') }}">
                                            </div>
                                            @error('district')
                                            <p class='text-danger inputerror'>{{ $message }} </p>
                                            @enderror
                                           
                                            </div>
                                            <div class="text-center">
                                                <button type="submit"
                                                    class="btn btn-lg bg-gradient-primary btn-lg w-100 mt-4 mb-0">Submit</button>
                                            </div>
                                        </form>
                                 
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </section>
        </main>
    </div>

    @push('js')
    <script src="{{ asset('assets') }}/js/jquery.min.js"></script>
    <script>
        $(function() {
    
        var text_val = $(".input-group input").val();
        if (text_val === "") {
          $(".input-group").removeClass('is-filled');
        } else {
          $(".input-group").addClass('is-filled');
        }
    });
    </script>
    @endpush
</x-layout>

