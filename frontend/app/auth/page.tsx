import Overlay from "./_components/Overlay";
import AuthForm from "./_components/AuthForm";

export default function Home() {
  const loginFields = [
    {
      name: "email",
      type: "text",
      placeholder: "Email",
    },
    {
      name: "password",
      type: "password",
      placeholder: "Password",
    },
  ];

  const signupFields = [
    {
      name: "email",
      type: "email",
      placeholder: "Email",
    },
    {
      name: "password",
      type: "password",
      placeholder: "Password",
    },
  ];

  return (
    <div className="bg-white md:size-2/3 size-4/5 rounded flex md:flex-row flex-col relative overflow-hidden">
      <Overlay />
      <AuthForm
        title="Sign In"
        subtitle="use your email and password"
        buttonText="SIGN IN"
        fields={loginFields}
        isLogin={true}
      />
      <AuthForm
        title="Create Account"
        subtitle="use your email for registration"
        buttonText="SIGN UP"
        fields={signupFields}
        isLogin={false}
      />
    </div>
  );
}
