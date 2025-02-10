package com.example.appsforreading.data.network

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient

object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = "https://nzwhnsgeqbfcylwhchwb.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im56d2huc2dlcWJmY3lsd2hjaHdiIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzY3NDE5MTgsImV4cCI6MjA1MjMxNzkxOH0.J_MFAF_CVG4VdXy8FueRv8n_8JWndRMTodEH86PGJHQ"
    ) {
        install(Auth)
    }
}